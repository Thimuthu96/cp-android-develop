/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.roomselect.impl

import cloud.orelit.communicationplatform.libraries.matrix.ui.model.SelectRoomInfo

sealed interface RoomSelectEvent {
    data class ToggleSelectedRoom(val room: SelectRoomInfo) : RoomSelectEvent
    data object ToggleSearchActive : RoomSelectEvent
    data class UpdateVisibleRange(val range: IntRange) : RoomSelectEvent
}
