/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.model

import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomSummary
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class SelectRoomInfo(
    val roomId: RoomId,
    val name: String?,
    val canonicalAlias: RoomAlias?,
    val avatarUrl: String?,
    val heroes: ImmutableList<MatrixUser>,
    val isDm: Boolean,
    val isTombstoned: Boolean,
) {
    fun getAvatarData(size: AvatarSize) = AvatarData(
        id = roomId.value,
        name = name,
        url = avatarUrl,
        size = size,
    )
}

fun RoomSummary.toSelectRoomInfo() = info.toSelectRoomInfo()

fun RoomInfo.toSelectRoomInfo() = SelectRoomInfo(
    roomId = id,
    name = name,
    avatarUrl = avatarUrl,
    heroes = heroes,
    canonicalAlias = canonicalAlias,
    isDm = isDm,
    isTombstoned = successorRoom != null,
)
