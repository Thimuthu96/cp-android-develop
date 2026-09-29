/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.deeplink.api

import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId

sealed interface DeeplinkData {
    /** Session id is common for all deep links. */
    val sessionId: SessionId

    /** The target is the root of the app, with the given [sessionId]. */
    data class Root(override val sessionId: SessionId) : DeeplinkData

    /** The target is a room, with the given [sessionId], [roomId] and optionally a [threadId] and [eventId]. */
    data class Room(override val sessionId: SessionId, val roomId: RoomId, val threadId: ThreadId?, val eventId: EventId?) : DeeplinkData
}
