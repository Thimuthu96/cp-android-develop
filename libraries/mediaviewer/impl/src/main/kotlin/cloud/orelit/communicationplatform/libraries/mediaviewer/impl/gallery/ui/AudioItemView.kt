/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.MediaItem

@Composable
fun AudioItemView(
    audio: MediaItem.Audio,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    GalleryFileListItem(
        modifier = modifier,
        contentValidationValue = contentValidationValue,
        caption = audio.mediaInfo.caption,
    ) {
        GalleryFileListItemContent(
            name = audio.mediaInfo.filename,
            formattedSize = audio.mediaInfo.formattedFileSize,
            icon = CompoundIcons.Audio(),
            isValidating = contentValidationValue.isLoading(),
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AudioItemViewPreview(
    @PreviewParameter(MediaItemAudioPreviewParam::class) audio: MediaItem.Audio,
) = ElementPreview {
    val states = remember {
        listOf(
            ContentValidationValue.Valid,
            ContentValidationValue.Loading,
            ContentValidationValue.Invalid,
            ContentValidationValue.UnrecoverableError(Throwable("Unrecoverable error")),
        )
    }
    Column {
        for (state in states) {
            AudioItemView(
                contentValidationValue = state,
                audio = audio,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
