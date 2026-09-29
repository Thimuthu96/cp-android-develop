/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaupload.api

import cloud.orelit.communicationplatform.libraries.androidutils.media.VideoCompressorHelper
import cloud.orelit.communicationplatform.libraries.preferences.api.store.VideoCompressionPreset

data class MediaOptimizationConfig(
    val compressImages: Boolean,
    val videoCompressionPreset: VideoCompressionPreset,
)

fun VideoCompressionPreset.compressorHelper(): VideoCompressorHelper = when (this) {
    VideoCompressionPreset.STANDARD -> VideoCompressorHelper(1280)
    VideoCompressionPreset.HIGH -> VideoCompressorHelper(1920)
    VideoCompressionPreset.LOW -> VideoCompressorHelper(640)
}
