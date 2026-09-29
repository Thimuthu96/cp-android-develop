/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.timeline.item.event

import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.EventThreadInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.AudioMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EmoteMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FileMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FormattedBody
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryItemType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.InReplyTo
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocationMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageFormat
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.NoticeMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.OtherMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.TextMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VideoMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VoiceMessageType
import cloud.orelit.communicationplatform.libraries.matrix.impl.media.map
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.location.into
import cloud.orelit.communicationplatform.libraries.matrix.impl.timeline.reply.InReplyToMapper
import org.matrix.rustcomponents.sdk.InReplyToDetails
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.MsgLikeKind
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.FormattedBody as RustFormattedBody
import org.matrix.rustcomponents.sdk.GalleryItemType as RustGalleryItemType
import org.matrix.rustcomponents.sdk.MessageFormat as RustMessageFormat
import org.matrix.rustcomponents.sdk.MessageType as RustMessageType

class EventMessageMapper {
    private val inReplyToMapper by lazy { InReplyToMapper(TimelineEventContentMapper()) }

    fun map(message: MsgLikeKind.Message, inReplyTo: InReplyToDetails?, threadInfo: EventThreadInfo?): MessageContent = message.use {
        val type = it.content.msgType.use(this::mapMessageType)
        val inReplyToEvent: InReplyTo? = inReplyTo?.use(inReplyToMapper::map)
        MessageContent(
            body = it.content.body,
            inReplyTo = inReplyToEvent,
            isEdited = it.content.isEdited,
            threadInfo = threadInfo,
            type = type
        )
    }

    fun mapMessageType(type: RustMessageType) = when (type) {
        is RustMessageType.Audio -> {
            when (type.content.voice) {
                null -> {
                    AudioMessageType(
                        filename = type.content.filename,
                        caption = type.content.caption,
                        formattedCaption = type.content.formattedCaption?.map(),
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                    )
                }
                else -> {
                    VoiceMessageType(
                        filename = type.content.filename,
                        caption = type.content.caption,
                        formattedCaption = type.content.formattedCaption?.map(),
                        source = type.content.source.map(),
                        info = type.content.info?.map(),
                        details = type.content.audio?.map(),
                    )
                }
            }
        }
        is RustMessageType.File -> {
            FileMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
        }
        is RustMessageType.Image -> {
            ImageMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
        }
        is RustMessageType.Notice -> {
            NoticeMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Text -> {
            TextMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Emote -> {
            EmoteMessageType(type.content.body, type.content.formatted?.map())
        }
        is RustMessageType.Video -> {
            VideoMessageType(
                filename = type.content.filename,
                caption = type.content.caption,
                formattedCaption = type.content.formattedCaption?.map(),
                source = type.content.source.map(),
                info = type.content.info?.map(),
            )
        }
        is RustMessageType.Location -> {
            LocationMessageType(
                body = type.content.body,
                geoUri = type.content.geoUri,
                description = type.content.description,
                assetType = type.content.asset.into()
            )
        }
        is MessageType.Other -> {
            OtherMessageType(type.msgtype, type.body)
        }
        is MessageType.Gallery -> {
            GalleryMessageType(
                body = type.content.body,
                formatted = type.content.formatted?.map(),
                items = type.content.itemtypes.map { mapGalleryItemType(it) },
            )
        }
    }

    private fun mapGalleryItemType(type: RustGalleryItemType): GalleryItemType = when (type) {
        is RustGalleryItemType.Image -> {
            GalleryItemType.Image(
                content = ImageMessageType(
                    filename = type.content.filename,
                    caption = type.content.caption,
                    formattedCaption = type.content.formattedCaption?.map(),
                    source = type.content.source.map(),
                    info = type.content.info?.map(),
                )
            )
        }
        is RustGalleryItemType.Audio -> {
            GalleryItemType.Audio(
                content = AudioMessageType(
                    filename = type.content.filename,
                    caption = type.content.caption,
                    formattedCaption = type.content.formattedCaption?.map(),
                    source = type.content.source.map(),
                    info = type.content.info?.map(),
                )
            )
        }
        is RustGalleryItemType.Video -> {
            GalleryItemType.Video(
                content = VideoMessageType(
                    filename = type.content.filename,
                    caption = type.content.caption,
                    formattedCaption = type.content.formattedCaption?.map(),
                    source = type.content.source.map(),
                    info = type.content.info?.map(),
                )
            )
        }
        is RustGalleryItemType.File -> {
            GalleryItemType.File(
                content = FileMessageType(
                    filename = type.content.filename,
                    caption = type.content.caption,
                    formattedCaption = type.content.formattedCaption?.map(),
                    source = type.content.source.map(),
                    info = type.content.info?.map(),
                )
            )
        }
        is RustGalleryItemType.Other -> {
            GalleryItemType.Other(
                itemType = type.itemtype,
                body = type.body,
            )
        }
    }
}

private fun RustFormattedBody.map(): FormattedBody = FormattedBody(
    format = format.map(),
    body = body
)

private fun RustMessageFormat.map(): MessageFormat {
    return when (this) {
        RustMessageFormat.Html -> MessageFormat.HTML
        is RustMessageFormat.Unknown -> MessageFormat.UNKNOWN
    }
}
