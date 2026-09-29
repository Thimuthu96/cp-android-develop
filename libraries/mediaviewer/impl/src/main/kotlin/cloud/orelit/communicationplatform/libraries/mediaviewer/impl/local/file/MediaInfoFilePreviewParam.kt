/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.local.file

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.aZipMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anApkMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anAudioMediaInfo

open class MediaInfoFilePreviewParam : PreviewParameterProvider<MediaInfo> {
    override val values: Sequence<MediaInfo>
        get() = sequenceOf(
            aZipMediaInfo(),
            anAudioMediaInfo(),
            anApkMediaInfo(),
        )
}
