/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

import android.media.projection.MediaProjection

/**
 * Obtains a [MediaProjection] after asking the user for consent, and keeps the foreground service in the
 * `mediaProjection` state while it is in use.
 */
interface ScreenCaptureProvider {
    /**
     * Ask the user for consent. [onResult] is invoked on the main thread with null if the user declined or an error occurred.
     */
    fun requestScreenCapture(onResult: (MediaProjection?) -> Unit)

    /**
     * Must be called once the [MediaProjection] returned by [requestScreenCapture] is not used anymore.
     */
    fun releaseScreenCapture()
}

internal object NoOpScreenCaptureProvider : ScreenCaptureProvider {
    override fun requestScreenCapture(onResult: (MediaProjection?) -> Unit) = onResult(null)
    override fun releaseScreenCapture() = Unit
}
