/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.appnavstate.api

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId

fun NavigationState.currentSessionId(): SessionId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> sessionId
        is NavigationState.Room -> parentSession.sessionId
        is NavigationState.Thread -> parentRoom.parentSession.sessionId
    }
}

fun NavigationState.currentRoomId(): RoomId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> null
        is NavigationState.Room -> roomId
        is NavigationState.Thread -> parentRoom.roomId
    }
}

fun NavigationState.currentThreadId(): ThreadId? {
    return when (this) {
        NavigationState.Root -> null
        is NavigationState.Session -> null
        is NavigationState.Room -> null
        is NavigationState.Thread -> threadId
    }
}
