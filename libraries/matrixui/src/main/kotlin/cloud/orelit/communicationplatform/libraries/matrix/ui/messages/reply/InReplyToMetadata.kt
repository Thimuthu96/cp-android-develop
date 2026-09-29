/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.AudioMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FileMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryItemType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LiveLocationContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocationMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RedactedContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VideoMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VoiceMessageType
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.AttachmentThumbnailInfo
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.AttachmentThumbnailType
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply.InReplyToMetadata.Text
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply.InReplyToMetadata.Thumbnail
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonPlurals
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Immutable
internal sealed interface InReplyToMetadata {
    data class Thumbnail(
        val attachmentThumbnailInfo: AttachmentThumbnailInfo
    ) : InReplyToMetadata {
        val text: String = attachmentThumbnailInfo.textContent.orEmpty()
    }

    data class Text(
        val text: String
    ) : InReplyToMetadata

    sealed interface Informative : InReplyToMetadata

    data object Redacted : Informative
    data object UnableToDecrypt : Informative
}

/**
 * Computes metadata for the in reply to message.
 *
 * Metadata can be either a thumbnail with a text OR just a text.
 */
@Composable
internal fun InReplyToDetails.Ready.metadata(hideImage: Boolean): InReplyToMetadata? = when (eventContent) {
    is MessageContent -> when (val type = eventContent.type) {
        is ImageMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = (type.info?.thumbnailSource ?: type.source).takeUnless { hideImage },
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Image,
                blurHash = type.info?.blurhash,
            )
        )
        is VideoMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = type.info?.thumbnailSource?.takeUnless { hideImage },
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Video,
                blurHash = type.info?.blurhash,
            )
        )
        is FileMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = type.info?.thumbnailSource?.takeUnless { hideImage },
                textContent = eventContent.body,
                type = AttachmentThumbnailType.File,
            )
        )
        is LocationMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                textContent = stringResource(CommonStrings.common_shared_location),
                type = AttachmentThumbnailType.Location,
            )
        )
        is AudioMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Audio,
            )
        )
        is VoiceMessageType -> Thumbnail(
            AttachmentThumbnailInfo(
                textContent = stringResource(CommonStrings.common_voice_message),
                type = AttachmentThumbnailType.Voice,
            )
        )
        is GalleryMessageType -> {
            val caption = textContent?.takeIf { it.isNotBlank() }
            val isMediaGallery = type.items.all { it is GalleryItemType.Image || it is GalleryItemType.Video }
            if (isMediaGallery) {
                val text = caption ?: pluralStringResource(
                    CommonPlurals.common_gallery_reply_media_items,
                    type.items.size,
                    type.items.size,
                )
                val firstMediaItem = type.items.firstOrNull { it is GalleryItemType.Image || it is GalleryItemType.Video }
                val thumbnailSource = when (firstMediaItem) {
                    is GalleryItemType.Image -> (firstMediaItem.content.info?.thumbnailSource ?: firstMediaItem.content.source).takeUnless { hideImage }
                    is GalleryItemType.Video -> firstMediaItem.content.info?.thumbnailSource?.takeUnless { hideImage }
                    else -> null
                }
                val blurHash = when (firstMediaItem) {
                    is GalleryItemType.Image -> firstMediaItem.content.info?.blurhash
                    is GalleryItemType.Video -> firstMediaItem.content.info?.blurhash
                    else -> null
                }
                val type = when (firstMediaItem) {
                    is GalleryItemType.Video -> AttachmentThumbnailType.Video
                    else -> AttachmentThumbnailType.Image
                }
                Thumbnail(
                    AttachmentThumbnailInfo(
                        thumbnailSource = thumbnailSource,
                        textContent = text,
                        type = type,
                        blurHash = blurHash,
                    )
                )
            } else {
                val text = caption ?: pluralStringResource(
                    CommonPlurals.common_gallery_reply_attachments,
                    type.items.size,
                    type.items.size,
                )
                Thumbnail(
                    AttachmentThumbnailInfo(
                        textContent = text,
                        type = AttachmentThumbnailType.File,
                    )
                )
            }
        }
        else -> Text(textContent ?: eventContent.body)
    }
    is StickerContent -> Thumbnail(
        AttachmentThumbnailInfo(
            thumbnailSource = eventContent.source.takeUnless { hideImage },
            textContent = eventContent.body,
            type = AttachmentThumbnailType.Image,
            blurHash = eventContent.info.blurhash,
        )
    )
    is PollContent -> Thumbnail(
        AttachmentThumbnailInfo(
            textContent = eventContent.question,
            type = AttachmentThumbnailType.Poll,
        )
    )
    is RedactedContent -> InReplyToMetadata.Redacted
    is UnableToDecryptContent -> InReplyToMetadata.UnableToDecrypt
    is FailedToParseMessageLikeContent,
    is FailedToParseStateContent,
    is ProfileChangeContent,
    is RoomMembershipContent,
    is StateContent,
    UnknownContent,
    is LegacyCallInviteContent,
    is CallNotifyContent,
    is LiveLocationContent,
    null -> null
}
