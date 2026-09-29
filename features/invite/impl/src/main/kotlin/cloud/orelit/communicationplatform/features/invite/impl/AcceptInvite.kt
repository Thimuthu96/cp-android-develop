/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invite.impl

import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.JoinedRoom
import cloud.orelit.communicationplatform.features.invite.api.SeenInvitesStore
import cloud.orelit.communicationplatform.libraries.core.extensions.mapFailure
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toRoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.exception.ClientException
import cloud.orelit.communicationplatform.libraries.matrix.api.exception.ErrorKind
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRoom
import cloud.orelit.communicationplatform.libraries.push.api.notifications.NotificationCleaner

interface AcceptInvite {
    suspend operator fun invoke(roomId: RoomId): Result<RoomId>

    sealed class Failures : Exception() {
        data object InvalidInvite : Failures()
    }
}

@ContributesBinding(SessionScope::class)
class DefaultAcceptInvite(
    private val client: MatrixClient,
    private val joinRoom: JoinRoom,
    private val notificationCleaner: NotificationCleaner,
    private val seenInvitesStore: SeenInvitesStore,
) : AcceptInvite {
    override suspend fun invoke(roomId: RoomId): Result<RoomId> {
        return joinRoom(
            roomIdOrAlias = roomId.toRoomIdOrAlias(),
            serverNames = emptyList(),
            trigger = JoinedRoom.Trigger.Invite,
        ).onSuccess {
            notificationCleaner.clearMembershipNotificationForRoom(client.sessionId, roomId)
            seenInvitesStore.markAsUnSeen(roomId)
        }.mapFailure {
            if (it is ClientException.MatrixApi) {
                when (it.kind) {
                    ErrorKind.Unknown -> AcceptInvite.Failures.InvalidInvite
                    else -> it
                }
            } else {
                it
            }
        }.map { roomId }
    }
}
