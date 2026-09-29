/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.call.impl.notifications.CallNotificationData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ROOM_NAME
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

open class CallNotificationDataPreviewParam : PreviewParameterProvider<CallNotificationData> {
    override val values: Sequence<CallNotificationData>
        get() = sequenceOf(
            aCallNotificationData(
                audioOnly = false
            ),
            aCallNotificationData(
                audioOnly = true
            ),
        )
}

internal fun aCallNotificationData(
    audioOnly: Boolean
): CallNotificationData {
    return CallNotificationData(
        sessionId = SessionId("@alice:matrix.org"),
        roomId = RoomId("!1234:matrix.org"),
        eventId = EventId("\$asdadadsad:matrix.org"),
        senderId = UserId("@bob:matrix.org"),
        roomName = ROOM_NAME,
        senderName = USER_NAME_BOB,
        avatarUrl = null,
        notificationChannelId = "incoming_call",
        timestamp = 0L,
        textContent = null,
        expirationTimestamp = 1000L,
        audioOnly = audioOnly
    )
}
