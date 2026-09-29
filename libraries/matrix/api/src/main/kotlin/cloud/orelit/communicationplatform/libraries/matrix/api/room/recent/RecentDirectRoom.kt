/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.api.room.recent

import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.toMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class RecentDirectRoom(
    val roomId: RoomId,
    val matrixUser: MatrixUser,
)

/**
 * Returns a [Flow] of [RecentDirectRoom] from recently visited DM rooms.
 * The flow emits items lazily, allowing callers to filter and take only what they need.
 * Use [kotlinx.coroutines.flow.take] to limit results and stop iteration early.
 */
fun MatrixClient.getRecentDirectRooms(): Flow<RecentDirectRoom> = flow {
    val foundUserIds = mutableSetOf<UserId>()
    val recentlyVisitedRooms = getRecentlyVisitedRooms().getOrDefault(emptyList())
    for (roomId in recentlyVisitedRooms) {
        getRoom(roomId)?.use { room ->
            val info = room.info()
            if (info.isDm && info.currentUserMembership == CurrentUserMembership.JOINED) {
                val otherUser = room.getDirectRoomMember()?.toMatrixUser()
                if (otherUser != null && foundUserIds.add(otherUser.userId)) {
                    emit(RecentDirectRoom(room.roomId, otherUser))
                }
            }
        }
    }
}
