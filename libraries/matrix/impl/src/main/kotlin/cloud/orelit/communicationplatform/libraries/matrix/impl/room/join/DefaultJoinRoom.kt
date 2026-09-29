/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.room.join

import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.JoinedRoom
import cloud.orelit.communicationplatform.libraries.core.extensions.mapFailure
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.exception.ClientException
import cloud.orelit.communicationplatform.libraries.matrix.api.exception.ErrorKind
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRoom
import cloud.orelit.communicationplatform.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesBinding(SessionScope::class)
class DefaultJoinRoom(
    private val client: MatrixClient,
    private val analyticsService: AnalyticsService,
) : JoinRoom {
    override suspend fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger
    ): Result<Unit> {
        return when (roomIdOrAlias) {
            is RoomIdOrAlias.Id -> {
                if (serverNames.isEmpty()) {
                    client.joinRoom(roomIdOrAlias.roomId)
                } else {
                    client.joinRoomByIdOrAlias(roomIdOrAlias, serverNames)
                }
            }
            is RoomIdOrAlias.Alias -> {
                client.joinRoomByIdOrAlias(roomIdOrAlias, serverNames = emptyList())
            }
        }.onSuccess { roomInfo ->
            if (roomInfo != null) {
                analyticsService.capture(roomInfo.toAnalyticsJoinedRoom(trigger))
            }
        }.mapFailure {
            if (it is ClientException.MatrixApi) {
                when (it.kind) {
                    ErrorKind.Forbidden -> JoinRoom.Failures.UnauthorizedJoin
                    else -> it
                }
            } else {
                it
            }
        }.map { }
    }
}
