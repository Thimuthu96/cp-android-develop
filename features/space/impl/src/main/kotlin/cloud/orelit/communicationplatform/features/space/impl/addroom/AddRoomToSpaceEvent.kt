/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.addroom

import cloud.orelit.communicationplatform.libraries.matrix.ui.model.SelectRoomInfo

sealed interface AddRoomToSpaceEvent {
    data class ToggleRoom(val room: SelectRoomInfo) : AddRoomToSpaceEvent
    data class OnSearchActiveChanged(val active: Boolean) : AddRoomToSpaceEvent
    data object Save : AddRoomToSpaceEvent
    data object ResetSaveAction : AddRoomToSpaceEvent
    data object Dismiss : AddRoomToSpaceEvent
    data class UpdateSearchVisibleRange(val range: IntRange) : AddRoomToSpaceEvent
}
