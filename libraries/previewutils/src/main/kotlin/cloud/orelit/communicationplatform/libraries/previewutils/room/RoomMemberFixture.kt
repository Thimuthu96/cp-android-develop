/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.previewutils.room

import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_CAROL
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_DAVID
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_EVE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_JUSTIN
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_MALLORY
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_SUSIE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_VICTOR
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_WALTER
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import kotlinx.collections.immutable.persistentListOf

fun aRoomMember(
    userId: UserId = UserId("@alice:server.org"),
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    isIgnored: Boolean = false,
    role: RoomMember.Role = RoomMember.Role.User,
    membershipChangeReason: String? = null,
    isServiceMember: Boolean = false,
    displayedStatus: DisplayedStatus? = null,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    isIgnored = isIgnored,
    role = role,
    membershipChangeReason = membershipChangeReason,
    isServiceMember = isServiceMember,
    displayedStatus = displayedStatus,
)

fun aRoomMemberList() = persistentListOf(
    anAlice(),
    aBob(),
    aRoomMember(UserId("@carol:server.org"), USER_NAME_CAROL),
    aRoomMember(UserId("@david:server.org"), USER_NAME_DAVID),
    aRoomMember(UserId("@eve:server.org"), USER_NAME_EVE),
    aRoomMember(UserId("@justin:server.org"), USER_NAME_JUSTIN),
    aRoomMember(UserId("@mallory:server.org"), USER_NAME_MALLORY),
    aRoomMember(UserId("@susie:server.org"), USER_NAME_SUSIE),
    aVictor(),
    aWalter(),
)

fun anAlice() = aRoomMember(UserId("@alice:server.org"), USER_NAME_ALICE, role = RoomMember.Role.Admin)
fun aBob() = aRoomMember(UserId("@bob:server.org"), USER_NAME_BOB, role = RoomMember.Role.Moderator)

fun aVictor() = aRoomMember(
    UserId("@victor:server.org"),
    USER_NAME_VICTOR,
    membership = RoomMembershipState.INVITE
)

fun aWalter() = aRoomMember(
    UserId("@walter:server.org"),
    USER_NAME_WALTER,
    membership = RoomMembershipState.INVITE
)
