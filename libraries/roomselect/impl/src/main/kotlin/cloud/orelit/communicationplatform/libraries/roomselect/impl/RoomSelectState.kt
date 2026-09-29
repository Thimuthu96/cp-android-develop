/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.roomselect.impl

import androidx.compose.foundation.text.input.TextFieldState
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBarResultState
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.SelectRoomInfo
import cloud.orelit.communicationplatform.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList

data class RoomSelectState(
    val mode: RoomSelectMode,
    val maxNumberOfRooms: Int,
    val resultState: SearchBarResultState<ImmutableList<SelectRoomInfo>>,
    val searchQuery: TextFieldState,
    val isSearchActive: Boolean,
    val selectedRooms: ImmutableList<SelectRoomInfo>,
    val eventSink: (RoomSelectEvent) -> Unit,
) {
    val canSelectMoreRooms = selectedRooms.size < maxNumberOfRooms
}
