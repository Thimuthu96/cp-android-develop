/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import androidx.core.graphics.createBitmap
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt

/** Longest side of the captured frames. Keeps encoding cheap and the payload small while text stays readable. */
private const val MAX_CAPTURE_LONG_SIDE = 1280

/** Upper bound of the frame rate sent to the WebView. */
internal const val MAX_FRAMES_PER_SECOND = 12
private const val FRAME_INTERVAL_MILLIS = 1000L / MAX_FRAMES_PER_SECOND
private const val JPEG_QUALITY = 70

internal data class CaptureSize(val width: Int, val height: Int)

/**
 * Scale the screen size down so that its longest side is at most [maxLongSide], keeping the aspect ratio and even dimensions,
 * as required by most video encoders.
 */
internal fun computeCaptureSize(screenWidth: Int, screenHeight: Int, maxLongSide: Int = MAX_CAPTURE_LONG_SIDE): CaptureSize {
    val scale = (maxLongSide.toFloat() / max(screenWidth, screenHeight)).coerceAtMost(1f)
    fun Int.scaledEven() = ((this * scale).roundToInt() and 1.inv()).coerceAtLeast(2)
    return CaptureSize(screenWidth.scaledEven(), screenHeight.scaledEven())
}

/**
 * Mirrors the screen into a scaled-down [VirtualDisplay], and publishes JPEG frames to [frameSlot].
 *
 * Performance notes:
 * - All the work happens on a dedicated background thread, never on the main thread.
 * - Scaling is done by the compositor when rendering into the virtual display, not on the CPU.
 * - Frames are produced only when the screen content changes, at most [MAX_FRAMES_PER_SECOND] per second, and are dropped
 *   while the consumer has not fetched the previous one, so work never accumulates.
 * - Bitmaps and the encoding buffer are reused between frames to avoid GC pressure.
 */
internal class ScreenShareCapturer(
    private val mediaProjection: MediaProjection,
    screenWidth: Int,
    screenHeight: Int,
    private val densityDpi: Int,
    private val frameSlot: ScreenShareFrameSlot,
    private val onProjectionStopped: () -> Unit,
) {
    var captureSize = computeCaptureSize(screenWidth, screenHeight)
        private set

    private val thread = HandlerThread("ScreenShareCapture").apply { start() }
    private val handler = Handler(thread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())

    // The fields below are only accessed from the capture thread.
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var pendingImage: Image? = null
    private var isEncodeScheduled = false
    private var lastEncodeAt = 0L
    private var paddedBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var rowCopyBuffer: ByteArray? = null
    private val jpegStream = ByteArrayOutputStream()
    private var isReleased = false

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Timber.d("Screen share: media projection stopped")
            release()
            mainHandler.post(onProjectionStopped)
        }

        override fun onCapturedContentResize(width: Int, height: Int) {
            // Called on API 34+ when the captured content changes size, for instance on rotation or when sharing a single app.
            resize(width, height)
        }
    }

    private val encodeRunnable = Runnable { encodePendingImage() }

    /**
     * Start the capture. Must be called once. Throws if the virtual display cannot be created.
     */
    fun start() {
        // Since Android 14, the callback must be registered before creating the virtual display.
        mediaProjection.registerCallback(projectionCallback, handler)
        val reader = createImageReader(captureSize)
        imageReader = reader
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ElementCallScreenShare",
            captureSize.width,
            captureSize.height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            handler,
        )
    }

    /**
     * Stop the capture and release all the resources. Safe to call several times.
     */
    fun stop() {
        handler.post {
            mediaProjection.unregisterCallback(projectionCallback)
            release()
            mediaProjection.stop()
        }
        thread.quitSafely()
    }

    private fun createImageReader(size: CaptureSize): ImageReader {
        // 3 buffers: one held as the pending image, and 2 needed by acquireLatestImage() to skip stale frames.
        return ImageReader.newInstance(size.width, size.height, PixelFormat.RGBA_8888, 3).apply {
            setOnImageAvailableListener(::onImageAvailable, handler)
        }
    }

    private fun resize(contentWidth: Int, contentHeight: Int) {
        if (isReleased) return
        val newSize = computeCaptureSize(contentWidth, contentHeight)
        if (newSize == captureSize) return
        Timber.d("Screen share: resizing capture to ${newSize.width}x${newSize.height}")
        captureSize = newSize
        val newReader = createImageReader(newSize)
        virtualDisplay?.resize(newSize.width, newSize.height, densityDpi)
        virtualDisplay?.surface = newReader.surface
        pendingImage?.close()
        pendingImage = null
        imageReader?.close()
        imageReader = newReader
    }

    private fun onImageAvailable(reader: ImageReader) {
        if (isReleased || reader !== imageReader) return
        val image = try {
            reader.acquireLatestImage()
        } catch (e: IllegalStateException) {
            Timber.w(e, "Screen share: unable to acquire image")
            null
        } ?: return
        pendingImage?.close()
        pendingImage = image
        scheduleEncode(delayMillis = (lastEncodeAt + FRAME_INTERVAL_MILLIS - SystemClock.uptimeMillis()).coerceAtLeast(0))
    }

    private fun scheduleEncode(delayMillis: Long) {
        if (isEncodeScheduled) return
        isEncodeScheduled = true
        handler.postDelayed(encodeRunnable, delayMillis)
    }

    private fun encodePendingImage() {
        isEncodeScheduled = false
        if (isReleased) return
        if (pendingImage == null) return
        if (frameSlot.hasUnconsumedFrame()) {
            // The WebView is behind: keep the latest image and retry later, instead of encoding frames nobody will see.
            scheduleEncode(FRAME_INTERVAL_MILLIS)
            return
        }
        val image = pendingImage ?: return
        pendingImage = null
        lastEncodeAt = SystemClock.uptimeMillis()
        try {
            frameSlot.publish(encode(image))
        } catch (e: Exception) {
            Timber.w(e, "Screen share: unable to encode frame")
        } finally {
            image.close()
        }
    }

    private fun encode(image: Image): ByteArray {
        val width = image.width
        val height = image.height
        val plane = image.planes[0]
        val rowStride = plane.rowStride
        val paddedWidth = rowStride / plane.pixelStride
        val padded = paddedBitmap?.takeIf { it.width == paddedWidth && it.height == height }
            ?: createBitmap(paddedWidth, height).also {
                paddedBitmap?.recycle()
                paddedBitmap = it
            }
        val buffer = plane.buffer.apply { rewind() }
        val expectedSize = rowStride * height
        if (buffer.remaining() >= expectedSize) {
            padded.copyPixelsFromBuffer(buffer)
        } else {
            // Some devices do not pad the last row, copy what is available into a buffer of the expected size.
            val copy = rowCopyBuffer?.takeIf { it.size == expectedSize } ?: ByteArray(expectedSize).also { rowCopyBuffer = it }
            buffer.get(copy, 0, buffer.remaining())
            padded.copyPixelsFromBuffer(ByteBuffer.wrap(copy))
        }
        val output = if (paddedWidth == width) {
            padded
        } else {
            val cropped = croppedBitmap?.takeIf { it.width == width && it.height == height }
                ?: createBitmap(width, height).also {
                    croppedBitmap?.recycle()
                    croppedBitmap = it
                }
            // Drawing into the smaller bitmap drops the row padding without allocating a new bitmap.
            Canvas(cropped).drawBitmap(padded, 0f, 0f, null)
            cropped
        }
        jpegStream.reset()
        output.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, jpegStream)
        return jpegStream.toByteArray()
    }

    private fun release() {
        if (isReleased) return
        isReleased = true
        handler.removeCallbacks(encodeRunnable)
        virtualDisplay?.release()
        virtualDisplay = null
        pendingImage?.close()
        pendingImage = null
        imageReader?.close()
        imageReader = null
        paddedBitmap?.recycle()
        paddedBitmap = null
        croppedBitmap?.recycle()
        croppedBitmap = null
    }
}
