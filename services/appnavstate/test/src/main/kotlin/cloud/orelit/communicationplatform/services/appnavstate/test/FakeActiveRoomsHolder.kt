/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.appnavstate.test

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.services.appnavstate.api.ActiveRoomsHolder

class FakeActiveRoomsHolder : ActiveRoomsHolder {
    private var room: JoinedRoom? = null

    override fun addRoom(room: JoinedRoom) {
        this.room = room
    }

    override fun getActiveRoom(sessionId: SessionId): JoinedRoom? {
        return room
    }

    override fun getActiveRoomMatching(sessionId: SessionId, roomId: RoomId): JoinedRoom? {
        return null
    }

    override fun removeRoom(sessionId: SessionId, roomId: RoomId) {
        room = null
    }

    override fun clear(sessionId: SessionId) {
    }
}
