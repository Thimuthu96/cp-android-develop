/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.util.UUID

private const val BRIDGE_NAME = "elementXScreenShareBridge"
private const val JS_CALLBACKS = "window.__elementXScreenShare"
private const val FRAME_PATH = "/element-x-screen-share/frame"
private const val FRAME_URL = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}$FRAME_PATH"

/**
 * How long a frame request is held when the screen does not change. On timeout, the last frame is sent again.
 * Using a long poll means the page needs no timers, so it keeps receiving frames even when timers are throttled
 * because the app is in the background.
 */
private const val FRAME_WAIT_MILLIS = 1000L

/**
 * Android WebView does not implement `navigator.mediaDevices.getDisplayMedia()`, so Element Call hides its screen share button.
 *
 * This controller injects a polyfill of `getDisplayMedia()` before Element Call is loaded. When called, it asks the user for
 * a [android.media.projection.MediaProjection], captures the screen natively with [ScreenShareCapturer], and exposes the frames
 * through [interceptRequest]. The page draws them on a canvas and returns the canvas stream to Element Call, which publishes it
 * like on the web, so the encryption and the rest of the call are unchanged.
 *
 * Nothing runs until the user starts a screen share: the polyfill only defines a function.
 */
internal class WebViewScreenShareController(
    private val webView: WebView,
    private val isEnabled: () -> Boolean,
    private val screenCaptureProvider: ScreenCaptureProvider,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val token = UUID.randomUUID().toString()

    // Only accessed on the main thread
    private var capturer: ScreenShareCapturer? = null
    private var isStarting = false
    private var isDisposed = false

    // Read from the WebView request thread
    @Volatile
    private var frameSlot: ScreenShareFrameSlot? = null

    init {
        // The polyfill must be defined before Element Call checks for `getDisplayMedia`, so only enable the feature
        // when the script can be injected at document start. Otherwise, the behavior is unchanged (no screen share button).
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            webView.addJavascriptInterface(Bridge(), BRIDGE_NAME)
            WebViewCompat.addDocumentStartJavaScript(webView, POLYFILL, setOf("*"))
        } else {
            Timber.w("Screen share: DOCUMENT_START_SCRIPT is not supported by this WebView")
        }
    }

    /**
     * Serve the latest frame to the page. Called by the WebView on a background thread; it may block up to [FRAME_WAIT_MILLIS].
     * Returns null if the request is not for screen sharing.
     */
    fun interceptRequest(url: Uri): WebResourceResponse? {
        if (url.host != WebViewAssetLoader.DEFAULT_DOMAIN || url.path != FRAME_PATH) return null
        if (url.getQueryParameter("token") != token) return emptyResponse(403, "Forbidden")
        val slot = frameSlot ?: return emptyResponse(410, "Gone")
        return when (val result = slot.awaitFrame(FRAME_WAIT_MILLIS)) {
            is ScreenShareFrameSlot.Result.Frame -> WebResourceResponse(
                "image/jpeg",
                null,
                200,
                "OK",
                RESPONSE_HEADERS,
                ByteArrayInputStream(result.bytes),
            )
            ScreenShareFrameSlot.Result.NoFrame -> emptyResponse(204, "No Content")
            ScreenShareFrameSlot.Result.Closed -> emptyResponse(410, "Gone")
        }
    }

    /**
     * Stop any ongoing screen share. Must be called when the WebView is destroyed.
     */
    fun dispose() {
        isDisposed = true
        stopCapture(notifyWebView = false)
    }

    private fun startCapture() {
        if (capturer != null || isStarting) {
            evaluateCallback("onError('Screen sharing is already in progress')")
            return
        }
        isStarting = true
        screenCaptureProvider.requestScreenCapture { mediaProjection ->
            isStarting = false
            if (isDisposed) {
                // The call screen was closed while the user was being asked for consent
                mediaProjection?.stop()
                screenCaptureProvider.releaseScreenCapture()
                return@requestScreenCapture
            }
            if (mediaProjection == null) {
                evaluateCallback("onError('Permission denied')")
                return@requestScreenCapture
            }
            val (width, height) = webView.context.screenSize()
            val slot = ScreenShareFrameSlot()
            val newCapturer = ScreenShareCapturer(
                mediaProjection = mediaProjection,
                screenWidth = width,
                screenHeight = height,
                densityDpi = webView.resources.displayMetrics.densityDpi,
                frameSlot = slot,
                onProjectionStopped = { stopCapture(notifyWebView = true) },
            )
            try {
                newCapturer.start()
            } catch (e: Exception) {
                Timber.e(e, "Screen share: unable to start the capture")
                newCapturer.stop()
                screenCaptureProvider.releaseScreenCapture()
                evaluateCallback("onError('Unable to capture the screen')")
                return@requestScreenCapture
            }
            frameSlot = slot
            capturer = newCapturer
            val size = newCapturer.captureSize
            Timber.d("Screen share: started with size ${size.width}x${size.height}")
            evaluateCallback("onStarted('$FRAME_URL?token=$token', ${size.width}, ${size.height})")
        }
    }

    private fun stopCapture(notifyWebView: Boolean) {
        val currentCapturer = capturer ?: return
        capturer = null
        frameSlot?.close()
        frameSlot = null
        currentCapturer.stop()
        screenCaptureProvider.releaseScreenCapture()
        Timber.d("Screen share: stopped")
        if (notifyWebView) {
            evaluateCallback("onStopped()")
        }
    }

    private fun evaluateCallback(call: String) {
        webView.evaluateJavascript("$JS_CALLBACKS && $JS_CALLBACKS.$call", null)
    }

    private inner class Bridge {
        @JavascriptInterface
        fun isAvailable(): Boolean = isEnabled()

        @JavascriptInterface
        fun start() {
            mainHandler.post { startCapture() }
        }

        @JavascriptInterface
        fun stop() {
            mainHandler.post { stopCapture(notifyWebView = false) }
        }
    }

    private companion object {
        val RESPONSE_HEADERS = mapOf(
            // The page may be served from another origin when a custom Element Call URL is configured.
            "Access-Control-Allow-Origin" to "*",
            "Cache-Control" to "no-store",
        )

        fun emptyResponse(statusCode: Int, reasonPhrase: String) = WebResourceResponse(
            "text/plain",
            null,
            statusCode,
            reasonPhrase,
            RESPONSE_HEADERS,
            ByteArrayInputStream(ByteArray(0)),
        )

        /**
         * Polyfill of `navigator.mediaDevices.getDisplayMedia()`, backed by the native screen capture.
         * The returned track comes from a canvas, on which the frames fetched from [FRAME_URL] are drawn.
         */
        val POLYFILL = """
            (function () {
              const bridge = window.$BRIDGE_NAME;
              if (!bridge || !navigator.mediaDevices || "getDisplayMedia" in navigator.mediaDevices) return;
              if (!bridge.isAvailable()) return;

              let pending = null;
              let session = null;

              function endSession(notifyNative, dispatchEnded) {
                const s = session;
                if (!s) return;
                session = null;
                s.active = false;
                if (notifyNative) bridge.stop();
                s.originalStop();
                // Lets LiveKit unpublish the track when the capture is stopped from the system UI.
                if (dispatchEnded) s.track.dispatchEvent(new Event("ended"));
              }

              async function pump(s, canvas, context, frameUrl) {
                while (s.active) {
                  try {
                    // The native side holds the request until a new frame is available, so no timer is needed here.
                    const response = await fetch(frameUrl, { cache: "no-store" });
                    if (!s.active) break;
                    if (response.status === 204) continue;
                    if (response.status !== 200) {
                      endSession(false, true);
                      break;
                    }
                    const bitmap = await createImageBitmap(await response.blob());
                    if (s.active) {
                      if (canvas.width !== bitmap.width || canvas.height !== bitmap.height) {
                        canvas.width = bitmap.width;
                        canvas.height = bitmap.height;
                      }
                      context.drawImage(bitmap, 0, 0);
                    }
                    bitmap.close();
                  } catch (e) {
                    console.warn("Screen share: unable to render frame: " + e);
                    await new Promise((resolve) => setTimeout(resolve, 500));
                  }
                }
              }

              window.__elementXScreenShare = {
                onStarted(frameUrl, width, height) {
                  const p = pending;
                  pending = null;
                  if (!p) {
                    bridge.stop();
                    return;
                  }
                  // The canvas has the final size before the stream is returned, so that the encoder is configured for it.
                  const canvas = document.createElement("canvas");
                  canvas.width = width;
                  canvas.height = height;
                  const context = canvas.getContext("2d", { alpha: false });
                  context.fillRect(0, 0, width, height);
                  const stream = canvas.captureStream($MAX_FRAMES_PER_SECOND);
                  const track = stream.getVideoTracks()[0];
                  const s = { active: true, track: track, originalStop: track.stop.bind(track) };
                  track.stop = () => endSession(true, false);
                  session = s;
                  pump(s, canvas, context, frameUrl);
                  p.resolve(stream);
                },
                onError(message) {
                  const p = pending;
                  pending = null;
                  if (p) p.reject(new DOMException(message, "NotAllowedError"));
                },
                onStopped() {
                  endSession(false, true);
                },
              };

              navigator.mediaDevices.getDisplayMedia = function () {
                if (pending || session) {
                  return Promise.reject(new DOMException("Screen sharing is already in progress", "InvalidStateError"));
                }
                return new Promise((resolve, reject) => {
                  pending = { resolve: resolve, reject: reject };
                  bridge.start();
                });
              };
            })();
        """.trimIndent()
    }
}

private fun Context.screenSize(): Pair<Int, Int> {
    val windowManager = getSystemService(WindowManager::class.java)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.maximumWindowMetrics.bounds
        bounds.width() to bounds.height()
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        metrics.widthPixels to metrics.heightPixels
    }
}
