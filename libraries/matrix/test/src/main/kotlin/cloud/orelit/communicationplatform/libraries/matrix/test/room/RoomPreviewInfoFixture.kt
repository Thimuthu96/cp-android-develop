/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.room

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomType
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.api.room.preview.RoomPreviewInfo
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_TOPIC
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

fun aRoomPreview(
    localRoom: FakeBaseRoom? = null,
    info: RoomPreviewInfo = aRoomPreviewInfo(),
    roomMembershipDetails: () -> Result<RoomMembershipDetails?> = { lambdaError() },
) = FakeNotJoinedRoom(
    localRoom = localRoom,
    previewInfo = info,
    roomMembershipDetails = roomMembershipDetails,
)

fun aRoomPreviewInfo(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    joinRule: JoinRule = JoinRule.Public,
    isSpace: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    currentUserMembership: CurrentUserMembership? = null,
    numberOfJoinedMembers: Long = 1,
    isHistoryWorldReadable: Boolean = true,
) = RoomPreviewInfo(
    roomId = roomId,
    name = name,
    topic = topic,
    avatarUrl = avatarUrl,
    joinRule = joinRule,
    canonicalAlias = canonicalAlias,
    numberOfJoinedMembers = numberOfJoinedMembers,
    roomType = if (isSpace) RoomType.Space else RoomType.Room,
    isHistoryWorldReadable = isHistoryWorldReadable,
    membership = currentUserMembership,
)
