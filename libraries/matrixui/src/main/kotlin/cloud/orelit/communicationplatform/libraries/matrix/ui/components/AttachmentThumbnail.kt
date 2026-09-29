/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.components

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.MediaRequestData
import kotlinx.parcelize.Parcelize

@Composable
fun AttachmentThumbnail(
    info: AttachmentThumbnailInfo,
    modifier: Modifier = Modifier,
    thumbnailSize: Long = 32L,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
) {
    if (info.thumbnailSource != null) {
        val mediaRequestData = MediaRequestData(
            source = info.thumbnailSource,
            kind = MediaRequestData.Kind.Thumbnail(thumbnailSize),
        )
        BlurHashAsyncImage(
            model = mediaRequestData,
            blurHash = info.blurHash,
            contentDescription = info.textContent,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else if (info.blurHash != null) {
        BlurHashAsyncImage(
            model = null,
            blurHash = info.blurHash,
            contentDescription = info.textContent,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            when (info.type) {
                AttachmentThumbnailType.Image -> {
                    Icon(
                        imageVector = CompoundIcons.Image(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Video -> {
                    Icon(
                        imageVector = CompoundIcons.VideoCall(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Audio -> {
                    Icon(
                        imageVector = CompoundIcons.Audio(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Voice -> {
                    Icon(
                        imageVector = CompoundIcons.MicOnSolid(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.File -> {
                    Icon(
                        imageVector = CompoundIcons.Attachment(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Location -> {
                    Icon(
                        imageVector = CompoundIcons.LocationPin(),
                        contentDescription = info.textContent,
                    )
                }
                AttachmentThumbnailType.Poll -> {
                    Icon(
                        imageVector = CompoundIcons.Polls(),
                        contentDescription = info.textContent,
                    )
                }
            }
        }
    }
}

@Parcelize
enum class AttachmentThumbnailType : Parcelable {
    Image,
    Video,
    File,
    Audio,
    Location,
    Voice,
    Poll,
}

@Parcelize
data class AttachmentThumbnailInfo(
    val type: AttachmentThumbnailType,
    val thumbnailSource: MediaSource? = null,
    val textContent: String? = null,
    val blurHash: String? = null,
) : Parcelable

@PreviewsDayNight
@Composable
internal fun AttachmentThumbnailPreview(@PreviewParameter(AttachmentThumbnailInfoPreviewParam::class) data: AttachmentThumbnailInfo) = ElementPreview {
    AttachmentThumbnail(
        info = data,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(4.dp))
    )
}
