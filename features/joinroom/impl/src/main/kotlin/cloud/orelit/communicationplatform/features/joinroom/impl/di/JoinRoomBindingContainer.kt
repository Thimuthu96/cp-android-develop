/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.joinroom.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import im.vector.app.features.analytics.plan.JoinedRoom
import cloud.orelit.communicationplatform.features.invite.api.SeenInvitesStore
import cloud.orelit.communicationplatform.features.invite.api.acceptdecline.AcceptDeclineInviteState
import cloud.orelit.communicationplatform.features.joinroom.impl.JoinRoomPresenter
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRoom
import java.util.Optional

@BindingContainer
@ContributesTo(SessionScope::class)
object JoinRoomBindingContainer {
    @Provides
    fun providesJoinRoomPresenterFactory(
        client: MatrixClient,
        joinRoom: JoinRoom,
        knockRoom: KnockRoom,
        cancelKnockRoom: CancelKnockRoom,
        forgetRoom: ForgetRoom,
        acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
        buildMeta: BuildMeta,
        seenInvitesStore: SeenInvitesStore,
    ): JoinRoomPresenter.Factory {
        return object : JoinRoomPresenter.Factory {
            override fun create(
                roomId: RoomId,
                roomIdOrAlias: RoomIdOrAlias,
                roomDescription: Optional<RoomDescription>,
                serverNames: List<String>,
                trigger: JoinedRoom.Trigger,
            ): JoinRoomPresenter {
                return JoinRoomPresenter(
                    roomId = roomId,
                    roomIdOrAlias = roomIdOrAlias,
                    roomDescription = roomDescription,
                    serverNames = serverNames,
                    trigger = trigger,
                    matrixClient = client,
                    joinRoom = joinRoom,
                    knockRoom = knockRoom,
                    forgetRoom = forgetRoom,
                    cancelKnockRoom = cancelKnockRoom,
                    acceptDeclineInvitePresenter = acceptDeclineInvitePresenter,
                    buildMeta = buildMeta,
                    seenInvitesStore = seenInvitesStore,
                )
            }
        }
    }
}
