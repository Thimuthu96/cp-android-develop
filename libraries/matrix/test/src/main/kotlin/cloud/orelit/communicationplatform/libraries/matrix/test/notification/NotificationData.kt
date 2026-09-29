/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.notification

import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.NotificationContent
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.NotificationData
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_TIMESTAMP
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_NAME_2

fun aNotificationData(
    content: NotificationContent = NotificationContent.MessageLike.RoomEncrypted,
    isDirect: Boolean = false,
    isSpace: Boolean = false,
    hasMention: Boolean = false,
    threadId: ThreadId? = null,
    timestamp: Long = A_TIMESTAMP,
    senderDisplayName: String? = A_USER_NAME_2,
    senderIsNameAmbiguous: Boolean = false,
    roomDisplayName: String? = A_ROOM_NAME,
    roomJoinRule: JoinRule? = null,
    isNoisy: Boolean = false,
): NotificationData {
    return NotificationData(
        sessionId = A_SESSION_ID,
        eventId = AN_EVENT_ID,
        threadId = threadId,
        roomId = A_ROOM_ID,
        senderAvatarUrl = null,
        senderDisplayName = senderDisplayName,
        senderIsNameAmbiguous = senderIsNameAmbiguous,
        roomAvatarUrl = null,
        roomDisplayName = roomDisplayName,
        isDirect = isDirect,
        isDm = false,
        isSpace = isSpace,
        isEncrypted = false,
        isNoisy = isNoisy,
        timestamp = timestamp,
        content = content,
        hasMention = hasMention,
        roomJoinRule = roomJoinRule,
    )
}
