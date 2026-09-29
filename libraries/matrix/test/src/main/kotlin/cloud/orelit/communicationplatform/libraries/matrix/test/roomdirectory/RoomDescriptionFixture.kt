/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.roomdirectory

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.roomdirectory.RoomDescription
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID

fun aRoomDescription(
    roomId: RoomId = A_ROOM_ID,
    name: String? = null,
    topic: String? = null,
    alias: RoomAlias? = null,
    avatarUrl: String? = null,
    joinRule: RoomDescription.JoinRule = RoomDescription.JoinRule.UNKNOWN,
    isWorldReadable: Boolean = true,
    joinedMembers: Long = 2L
) = RoomDescription(
    roomId = roomId,
    name = name,
    topic = topic,
    alias = alias,
    avatarUrl = avatarUrl,
    joinRule = joinRule,
    isWorldReadable = isWorldReadable,
    numberOfMembers = joinedMembers
)
