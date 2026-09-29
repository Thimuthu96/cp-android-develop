/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.impl

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.core.extensions.DEFAULT_SAFE_LENGTH
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.eventformatter.api.RoomLatestEventFormatter
import cloud.orelit.communicationplatform.libraries.eventformatter.impl.mode.RenderingMode
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.LatestEventValue
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.AudioMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EmoteMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FileMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.GalleryMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LiveLocationContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocationMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.NoticeMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.OtherMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RedactedContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.TextMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VideoMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.VoiceMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.toPlainText
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

@ContributesBinding(SessionScope::class)
class DefaultRoomLatestEventFormatter(
    private val sp: StringProvider,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
    private val rtcNotificationContentFormatter: RtcNotificationContentFormatter,
    private val permalinkParser: PermalinkParser,
) : RoomLatestEventFormatter {
    override fun format(
        latestEvent: LatestEventValue.Local,
        isDmRoom: Boolean,
    ): CharSequence? = formatContent(
        content = latestEvent.content,
        isDmRoom = isDmRoom,
        isOutgoing = true,
        senderId = latestEvent.senderId,
        senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
    )

    override fun format(
        latestEvent: LatestEventValue.Remote,
        isDmRoom: Boolean,
    ): CharSequence? = formatContent(
        content = latestEvent.content,
        isDmRoom = isDmRoom,
        isOutgoing = latestEvent.isOwn,
        senderId = latestEvent.senderId,
        senderDisambiguatedDisplayName = latestEvent.senderProfile.getDisambiguatedDisplayName(latestEvent.senderId)
    )

    private fun formatContent(
        content: EventContent,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
        senderId: UserId,
        senderDisambiguatedDisplayName: String
    ): CharSequence? {
        return when (content) {
            is MessageContent -> content.process(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            is RedactedContent -> {
                val message = sp.getString(CommonStrings.common_message_removed)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is StickerContent -> {
                content.bestDescription.prefixWith(sp.getString(CommonStrings.common_sticker))
                    .prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is UnableToDecryptContent -> {
                val message = sp.getString(CommonStrings.common_waiting_for_decryption_key)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, senderId, senderDisambiguatedDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing, RenderingMode.RoomList)
            }
            is PollContent -> {
                content.question.prefixWith(sp.getString(CommonStrings.common_poll_summary_prefix))
                    .prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is FailedToParseMessageLikeContent, is FailedToParseStateContent, is UnknownContent -> {
                val message = sp.getString(CommonStrings.common_unsupported_event)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is LiveLocationContent -> {
                val message = sp.getString(CommonStrings.common_shared_live_location)
                message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
            }
            is LegacyCallInviteContent -> sp.getString(CommonStrings.common_unsupported_call)
            is CallNotifyContent -> rtcNotificationContentFormatter.format(content, isDmRoom)
        }?.take(DEFAULT_SAFE_LENGTH)
    }

    private fun MessageContent.process(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean
    ): CharSequence {
        val message = when (val messageType: MessageType = type) {
            // Doesn't need a prefix
            is EmoteMessageType -> {
                return "* ${senderDisambiguatedDisplayName.bidiIsolate()} ${messageType.body}"
            }
            is TextMessageType -> {
                messageType.toPlainText(permalinkParser)
            }
            is VideoMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_video))
            }
            is ImageMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_image))
            }
            is StickerMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_sticker))
            }
            is LocationMessageType -> {
                sp.getString(CommonStrings.common_shared_location)
            }
            is FileMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_file))
            }
            is AudioMessageType -> {
                messageType.toPlainText(permalinkParser).prefixWith(sp.getString(CommonStrings.common_audio))
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
                messageType.body.prefixWith(sp.getString(CommonStrings.common_gallery))
            }
            is NoticeMessageType -> {
                messageType.body
            }
        }
        return message.prefixIfNeeded(senderDisambiguatedDisplayName, isDmRoom, isOutgoing)
    }

    private fun CharSequence.prefixIfNeeded(
        senderDisambiguatedDisplayName: String,
        isDmRoom: Boolean,
        isOutgoing: Boolean,
    ): CharSequence = if (isDmRoom) {
        this
    } else {
        prefixWith(
            if (isOutgoing) {
                sp.getString(CommonStrings.common_you)
            } else {
                senderDisambiguatedDisplayName.bidiIsolate()
            }
        )
    }
}
