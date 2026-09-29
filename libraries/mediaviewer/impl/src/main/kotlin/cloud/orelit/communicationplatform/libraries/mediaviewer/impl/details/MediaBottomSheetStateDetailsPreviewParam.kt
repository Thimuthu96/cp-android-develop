/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.details

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anApkMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anImageMediaInfo

open class MediaBottomSheetStateDetailsPreviewParam : PreviewParameterProvider<MediaBottomSheetState.Details> {
    override val values: Sequence<MediaBottomSheetState.Details>
        get() = sequenceOf(
            aMediaBottomSheetStateDetails(),
            aMediaBottomSheetStateDetails(
                canDelete = false,
            ),
            aMediaBottomSheetStateDetails(
                mediaInfo = anApkMediaInfo(
                    dateSentFull = "December 6, 2024 at 12:59",
                ),
            ),
            aMediaBottomSheetStateDetails(
                eventId = null,
            ),
            aMediaBottomSheetStateDetails(
                fromGallery = true,
            ),
        )
}

fun aMediaBottomSheetStateDetails(
    fromGallery: Boolean = false,
    eventId: EventId? = EventId($$"$eventId"),
    canDelete: Boolean = true,
    mediaInfo: MediaInfo = anImageMediaInfo(
        senderName = USER_NAME_ALICE,
        dateSentFull = "December 6, 2024 at 12:59",
    ),
) = MediaBottomSheetState.Details(
    fromGallery = fromGallery,
    eventId = eventId,
    canDelete = canDelete,
    mediaInfo = mediaInfo,
    thumbnailSource = null,
)
