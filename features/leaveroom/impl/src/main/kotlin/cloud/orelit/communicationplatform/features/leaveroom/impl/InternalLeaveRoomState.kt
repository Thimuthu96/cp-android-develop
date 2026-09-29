/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.leaveroom.impl

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomEvent
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

data class InternalLeaveRoomState(
    val leaveAction: AsyncAction<Unit>,
    override val eventSink: (LeaveRoomEvent) -> Unit
) : LeaveRoomState

@Immutable
sealed interface Confirmation : AsyncAction.Confirming {
    data class Dm(val roomId: RoomId) : Confirmation
    data class Generic(val roomId: RoomId) : Confirmation
    data class PrivateRoom(val roomId: RoomId) : Confirmation
    data class LastUserInRoom(val roomId: RoomId) : Confirmation
    data class LastOwnerInRoom(val roomId: RoomId) : Confirmation
}
