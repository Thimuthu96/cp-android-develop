/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.room

import androidx.compose.runtime.Immutable
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
sealed interface LoadingRoomState {
    data object Loading : LoadingRoomState
    data object Error : LoadingRoomState
    data class Loaded(val room: JoinedRoom) : LoadingRoomState
}

open class LoadingRoomStatePreviewParam : PreviewParameterProvider<LoadingRoomState> {
    override val values: Sequence<LoadingRoomState>
        get() = sequenceOf(
            LoadingRoomState.Loading,
            LoadingRoomState.Error
        )
}

@Inject
class LoadingRoomStateFlowFactory(private val matrixClient: MatrixClient) {
    fun create(lifecycleScope: CoroutineScope, roomId: RoomId, joinedRoom: JoinedRoom?): StateFlow<LoadingRoomState> {
        return if (joinedRoom != null) {
            MutableStateFlow<LoadingRoomState>(LoadingRoomState.Loaded(joinedRoom))
        } else {
            getJoinedRoomFlow(roomId)
                .map { room ->
                    if (room != null) {
                        LoadingRoomState.Loaded(room)
                    } else {
                        LoadingRoomState.Error
                    }
                }
                .stateIn(lifecycleScope, SharingStarted.Eagerly, LoadingRoomState.Loading)
        }
    }

    private fun getJoinedRoomFlow(roomId: RoomId): Flow<JoinedRoom?> = suspend {
        matrixClient.getJoinedRoom(roomId = roomId)
    }
        .asFlow()
}
