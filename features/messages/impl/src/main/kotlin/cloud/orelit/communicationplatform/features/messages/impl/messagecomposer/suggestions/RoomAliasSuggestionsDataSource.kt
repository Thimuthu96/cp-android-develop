/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.suggestions

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomListService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RoomAliasSuggestion(
    val roomAlias: RoomAlias,
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatarUrl: String?,
)

interface RoomAliasSuggestionsDataSource {
    fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>>
}

@ContributesBinding(SessionScope::class)
class DefaultRoomAliasSuggestionsDataSource(
    private val roomListService: RoomListService,
) : RoomAliasSuggestionsDataSource {
    override fun getAllRoomAliasSuggestions(): Flow<List<RoomAliasSuggestion>> {
        return roomListService
            .allRooms
            .summaries
            .map { roomSummaries ->
                roomSummaries
                    .mapNotNull { roomSummary ->
                        roomSummary.info.canonicalAlias?.let { roomAlias ->
                            RoomAliasSuggestion(
                                roomAlias = roomAlias,
                                roomId = roomSummary.roomId,
                                roomName = roomSummary.info.name,
                                roomAvatarUrl = roomSummary.info.avatarUrl,
                            )
                        }
                    }
            }
    }
}
