/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anImageMediaInfo

open class MediaBottomSheetStateDeleteConfirmationPreviewParam : PreviewParameterProvider<MediaBottomSheetState.DeleteConfirmation> {
    override val values: Sequence<MediaBottomSheetState.DeleteConfirmation>
        get() = sequenceOf(
            aMediaBottomSheetStateDeleteConfirmation(),
            aMediaBottomSheetStateDeleteConfirmation(
                thumbnailSource = MediaSource("url_thumbnail")
            ),
        )
}

fun aMediaBottomSheetStateDeleteConfirmation(
    mediaInfo: MediaInfo = anImageMediaInfo(
        senderName = USER_NAME_ALICE,
    ),
    thumbnailSource: MediaSource? = null,
) = MediaBottomSheetState.DeleteConfirmation(
    eventId = EventId("\$eventId"),
    mediaInfo = mediaInfo,
    thumbnailSource = thumbnailSource,
)
