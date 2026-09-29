/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.test.attachments.video

import cloud.orelit.communicationplatform.features.messages.impl.attachments.video.MediaOptimizationSelectorPresenter
import cloud.orelit.communicationplatform.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia

class FakeMediaOptimizationSelectorPresenterFactory(
    private val fakePresenter: MediaOptimizationSelectorPresenter = MediaOptimizationSelectorPresenter {
        MediaOptimizationSelectorState(
            index = 0,
            maxUploadSize = AsyncData.Uninitialized,
            videoSizeEstimations = AsyncData.Uninitialized,
            isImageOptimizationEnabled = null,
            selectedVideoPreset = null,
            displayMediaSelectorViews = null,
            displayVideoPresetSelectorDialog = false,
            eventSink = {},
        )
    }
) : MediaOptimizationSelectorPresenter.Factory {
    override fun create(index: Int, localMedia: LocalMedia, sendAsFile: Boolean): MediaOptimizationSelectorPresenter {
        return fakePresenter
    }
}
