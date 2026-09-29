/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdirectory.impl.root.model

import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.libraries.matrix.api.roomdirectory.RoomDescription as MatrixRoomDescription

fun MatrixRoomDescription.toFeatureModel(): RoomDescription {
    return RoomDescription(
        roomId = roomId,
        name = name,
        alias = alias,
        topic = topic,
        avatarUrl = avatarUrl,
        numberOfMembers = numberOfMembers,
        joinRule = when (joinRule) {
            MatrixRoomDescription.JoinRule.PUBLIC -> RoomDescription.JoinRule.PUBLIC
            MatrixRoomDescription.JoinRule.KNOCK -> RoomDescription.JoinRule.KNOCK
            MatrixRoomDescription.JoinRule.RESTRICTED -> RoomDescription.JoinRule.RESTRICTED
            MatrixRoomDescription.JoinRule.KNOCK_RESTRICTED -> RoomDescription.JoinRule.KNOCK_RESTRICTED
            MatrixRoomDescription.JoinRule.INVITE -> RoomDescription.JoinRule.INVITE
            MatrixRoomDescription.JoinRule.UNKNOWN -> RoomDescription.JoinRule.UNKNOWN
        }
    )
}
