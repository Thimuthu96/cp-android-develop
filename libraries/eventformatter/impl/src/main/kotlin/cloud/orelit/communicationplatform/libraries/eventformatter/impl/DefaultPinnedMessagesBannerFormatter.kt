/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.impl

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.AudioMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EmoteMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FileMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocationMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.NoticeMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.OtherMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RedactedContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.TextMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VideoMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VoiceMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.toPlainText
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

@ContributesBinding(SessionScope::class)
class DefaultPinnedMessagesBannerFormatter(
    private val sp: StringProvider,
    private val permalinkParser: PermalinkParser,
) : PinnedMessagesBannerFormatter {
    override fun format(event: EventTimelineItem): CharSequence {
        return when (val content = event.content) {
            is MessageContent -> processMessageContents(event, content)
            is StickerContent -> {
                val text = content.body ?: content.filename
                text.prefixWith(CommonStrings.common_sticker)
            }
            is UnableToDecryptContent -> {
                sp.getString(CommonStrings.common_waiting_for_decryption_key)
            }
            is PollContent -> {
                content.question.prefixWith(CommonStrings.a11y_poll)
            }
            is RedactedContent -> {
                sp.getString(CommonStrings.common_message_removed)
            }
            else -> {
                sp.getString(CommonStrings.common_unsupported_event)
            }
        }
    }

    private fun processMessageContents(
        event: EventTimelineItem,
        messageContent: MessageContent,
    ): CharSequence {
        return when (val messageType: MessageType = messageContent.type) {
            is EmoteMessageType -> {
                val senderDisambiguatedDisplayName = event.senderProfile.getDisambiguatedDisplayName(event.sender)
                "* $senderDisambiguatedDisplayName ${messageType.body}"
            }
            is TextMessageType -> {
                messageType.toPlainText(permalinkParser)
            }
            is VideoMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(CommonStrings.common_video)
            }
            is ImageMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(CommonStrings.common_image)
            }
            is StickerMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(CommonStrings.common_sticker)
            }
            is LocationMessageType -> {
                messageType.body.prefixWith(CommonStrings.common_shared_location)
            }
            is FileMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(CommonStrings.common_file)
            }
            is AudioMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(CommonStrings.common_audio)
            }
            is VoiceMessageType -> {
                messageType
                    .toPlainText(permalinkParser, "")
                    .takeIf { it.isNotEmpty() }
                    ?.prefixWith(sp.getString(CommonStrings.common_voice_message))
                    ?: sp.getString(CommonStrings.common_voice_message)
            }
            is OtherMessageType -> {
                messageType.body
            }
            is GalleryMessageType -> {
                messageType.body.prefixWith(CommonStrings.common_gallery)
            }
            is NoticeMessageType -> {
                messageType.body
            }
        }
    }

    private fun CharSequence.prefixWith(@StringRes res: Int): AnnotatedString {
        val prefix = sp.getString(res)
        return prefixWith(prefix)
    }
}
