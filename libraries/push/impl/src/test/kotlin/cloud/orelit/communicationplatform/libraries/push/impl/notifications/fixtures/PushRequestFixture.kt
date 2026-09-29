/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications.fixtures

import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.push.impl.db.PushRequest
import cloud.orelit.communicationplatform.libraries.push.impl.push.PushRequestStatus

fun aPushRequest(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    providerInfo: String = "firebase",
    status: PushRequestStatus = PushRequestStatus.PENDING,
    retries: Int = 0,
) = PushRequest(
    pushDate = System.currentTimeMillis(),
    providerInfo = providerInfo,
    eventId = eventId.value,
    roomId = roomId.value,
    sessionId = sessionId.value,
    status = status.value,
    retries = retries.toLong(),
)
