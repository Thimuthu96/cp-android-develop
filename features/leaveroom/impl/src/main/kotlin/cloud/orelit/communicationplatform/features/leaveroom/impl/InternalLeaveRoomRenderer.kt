/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.leaveroom.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomRenderer
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomState
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

@ContributesBinding(SessionScope::class)
class InternalLeaveRoomRenderer : LeaveRoomRenderer {
    @Composable
    override fun Render(state: LeaveRoomState, onSelectNewOwners: (RoomId) -> Unit, modifier: Modifier) {
        if (state is InternalLeaveRoomState) {
            LeaveRoomView(state, onSelectNewOwners)
        } else {
            error("Unsupported state type ${state.javaClass}")
        }
    }
}
