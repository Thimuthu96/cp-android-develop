/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.features.home.impl.model.RoomListRoomSummary
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearchResult
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.AttachmentThumbnailType
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class GlobalSearchState(
    val isEnabled: Boolean,
    val isSearchActive: Boolean,
    val queryState: TextFieldState,
    val currentTarget: GlobalSearchTarget,
    val results: AsyncData<GlobalSearchResults>,
    val eventSink: (GlobalSearchEvent) -> Unit
)

@Immutable
enum class GlobalSearchTarget {
    ROOMS,
    MESSAGES,
}

@Immutable
sealed interface GlobalSearchResults {
    data class RoomListResults(val results: ImmutableList<RoomListRoomSummary>) : GlobalSearchResults
    data class MessageSearchResults(val results: ImmutableList<MessageSearchResultItem>) : GlobalSearchResults

    fun isEmpty(): Boolean = when (this) {
        is RoomListResults -> results.isEmpty()
        is MessageSearchResults -> results.isEmpty()
    }
}

sealed interface MessageSearchResultItem {
    val roomId: RoomId
    val eventId: EventId
    val senderId: UserId
    val senderName: String
    val roomInfo: RoomInfo
    val formattedTimestamp: String

    data class Message(
        val messageSearchResult: MessageSearchResult,
        val body: String,
        override val roomInfo: RoomInfo,
        override val formattedTimestamp: String,
    ) : MessageSearchResultItem {
        override val roomId: RoomId = messageSearchResult.roomId
        override val eventId: EventId = messageSearchResult.eventId
        override val senderId: UserId = messageSearchResult.senderId
        override val senderName: String = messageSearchResult.senderProfile.getDisambiguatedDisplayName(senderId)
    }

    data class Media(
        val messageSearchResult: MessageSearchResult,
        val mediaContent: MediaSearchResultContent,
        override val roomInfo: RoomInfo,
        override val formattedTimestamp: String,
    ) : MessageSearchResultItem {
        override val roomId: RoomId = messageSearchResult.roomId
        override val eventId: EventId = messageSearchResult.eventId
        override val senderId: UserId = messageSearchResult.senderId
        override val senderName: String = messageSearchResult.senderProfile.getDisambiguatedDisplayName(senderId)
    }
}

data class MediaSearchResultContent(
    val filename: String,
    val extension: String?,
    val caption: String?,
    val formattedSize: String?,
    val thumbnailSource: MediaSource?,
    val thumbnailType: AttachmentThumbnailType,
    val blurhash: String?,
)
