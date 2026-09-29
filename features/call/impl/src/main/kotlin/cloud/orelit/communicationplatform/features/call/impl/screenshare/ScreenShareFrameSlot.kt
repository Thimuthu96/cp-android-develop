/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

/**
 * Single-slot hand-off of encoded screen frames between the capture thread (producer) and the WebView (consumer).
 *
 * Only the most recent frame is kept, so a slow consumer never makes frames pile up in memory: the producer checks
 * [hasUnconsumedFrame] and skips encoding while the previous frame has not been fetched yet.
 */
internal class ScreenShareFrameSlot {
    sealed interface Result {
        class Frame(val bytes: ByteArray) : Result
        data object NoFrame : Result
        data object Closed : Result
    }

    private val lock = Object()
    private var latestFrame: ByteArray? = null
    private var hasUnconsumedFrame = false
    private var isClosed = false

    fun publish(frame: ByteArray) = synchronized(lock) {
        if (isClosed) return
        latestFrame = frame
        hasUnconsumedFrame = true
        lock.notifyAll()
    }

    fun hasUnconsumedFrame(): Boolean = synchronized(lock) { hasUnconsumedFrame }

    /**
     * Wait up to [timeoutMillis] for a frame that has not been consumed yet.
     * On timeout, the last frame is returned again (if any) so that the remote side keeps receiving a picture of a static screen,
     * which lets participants joining later get a key frame.
     */
    fun awaitFrame(timeoutMillis: Long): Result = synchronized(lock) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (!isClosed && !hasUnconsumedFrame) {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) break
            lock.wait(remaining)
        }
        if (isClosed) return Result.Closed
        hasUnconsumedFrame = false
        latestFrame?.let { Result.Frame(it) } ?: Result.NoFrame
    }

    fun close() = synchronized(lock) {
        isClosed = true
        latestFrame = null
        lock.notifyAll()
    }
}
