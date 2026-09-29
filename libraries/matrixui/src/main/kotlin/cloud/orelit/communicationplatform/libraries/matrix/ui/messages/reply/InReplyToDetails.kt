/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.InReplyTo
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.TextMessageType
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.toPlainText

@Immutable
sealed interface InReplyToDetails {
    data class Ready(
        val eventId: EventId,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
        val eventContent: EventContent?,
        val textContent: String?,
    ) : InReplyToDetails

    data class Loading(val eventId: EventId) : InReplyToDetails
    data class Error(val eventId: EventId, val message: String) : InReplyToDetails
}

fun InReplyToDetails.eventId() = when (this) {
    is InReplyToDetails.Ready -> eventId
    is InReplyToDetails.Loading -> eventId
    is InReplyToDetails.Error -> eventId
}

fun InReplyToDetails.content() = when (this) {
    is InReplyToDetails.Ready -> eventContent
    is InReplyToDetails.Loading -> null
    is InReplyToDetails.Error -> null
}

fun InReplyTo.map(
    permalinkParser: PermalinkParser,
) = when (this) {
    is InReplyTo.Ready -> InReplyToDetails.Ready(
        eventId = eventId,
        senderId = senderId,
        senderProfile = senderProfile,
        eventContent = content,
        textContent = when (content) {
            is MessageContent -> {
                val messageContent = content as MessageContent
                (messageContent.type as? TextMessageType)?.toPlainText(permalinkParser = permalinkParser) ?: messageContent.body
            }
            is StickerContent -> {
                val stickerContent = content as StickerContent
                stickerContent.body
            }
            else -> null
        }
    )
    is InReplyTo.Error -> InReplyToDetails.Error(eventId, message)
    is InReplyTo.NotLoaded -> InReplyToDetails.Loading(eventId)
    is InReplyTo.Pending -> InReplyToDetails.Loading(eventId)
}
