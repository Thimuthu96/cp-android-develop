/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.createroom.impl.configureroom

import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.AvatarAction

sealed interface ConfigureRoomEvent {
    data class RoomNameChanged(val name: String) : ConfigureRoomEvent
    data class TopicChanged(val topic: String) : ConfigureRoomEvent
    data class JoinRuleChanged(val joinRuleItem: JoinRuleItem) : ConfigureRoomEvent
    data class RoomAddressChanged(val roomAddress: String) : ConfigureRoomEvent
    data object CreateRoom : ConfigureRoomEvent
    data class HandleAvatarAction(val action: AvatarAction) : ConfigureRoomEvent
    data class SetParentSpace(val space: SpaceRoom?) : ConfigureRoomEvent
    data object CancelCreateRoom : ConfigureRoomEvent
}
