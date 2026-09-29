/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.room

import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.NotJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.room.preview.RoomPreviewInfo
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.member.RoomMemberMapper

class NotJoinedRustRoom(
    private val sessionId: SessionId,
    override val localRoom: RustBaseRoom?,
    override val previewInfo: RoomPreviewInfo,
) : NotJoinedRoom {
    override suspend fun membershipDetails(): Result<RoomMembershipDetails?> = runCatchingExceptions {
        val room = localRoom?.innerRoom ?: return@runCatchingExceptions null
        val (ownMember, senderInfo) = room.memberWithSenderInfo(sessionId.value)
        RoomMembershipDetails(
            currentUserMember = RoomMemberMapper.map(ownMember),
            senderMember = senderInfo?.let { RoomMemberMapper.map(it) },
        )
    }

    override fun close() {
        localRoom?.close()
    }
}
