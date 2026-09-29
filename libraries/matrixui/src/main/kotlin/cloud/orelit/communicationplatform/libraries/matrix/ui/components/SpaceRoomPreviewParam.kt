/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.preview.SPACE_NAME
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomType
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom
import cloud.orelit.communicationplatform.libraries.previewutils.room.aSpaceRoom

class SpaceRoomPreviewParam : PreviewParameterProvider<SpaceRoom> {
    override val values: Sequence<SpaceRoom> = sequenceOf(
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name with topic",
            topic = "Room topic that is quite long and might be truncated"
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name no topic",
            state = CurrentUserMembership.LEFT,
        ),
        aSpaceRoom(
            displayName = SPACE_NAME,
            roomType = RoomType.Room,
            isDirect = true,
            heroes = listOf(aMatrixUser(displayName = USER_NAME_ALICE)),
            state = CurrentUserMembership.JOINED,
            numJoinedMembers = 2,
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name with topic",
            topic = "Room topic that is quite long and might be truncated",
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            roomType = RoomType.Room,
            displayName = "Room name no topic",
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            roomId = RoomId("!spaceId0:example.com"),
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            roomId = RoomId("!spaceId1:example.com"),
            state = CurrentUserMembership.LEFT,
        ),
        aSpaceRoom(
            numJoinedMembers = 5,
            childrenCount = 10,
            worldReadable = true,
            avatarUrl = "anUrl",
            roomId = RoomId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
        aSpaceRoom(
            displayName = SPACE_NAME,
            roomType = RoomType.Space,
            heroes = listOf(aMatrixUser(displayName = USER_NAME_ALICE)),
            state = CurrentUserMembership.JOINED,
            numJoinedMembers = 2,
        ),
    )
}
