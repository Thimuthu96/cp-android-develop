/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x.di

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.appnav.di.RoomGraphFactory
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom

@ContributesBinding(SessionScope::class)
class DefaultRoomGraphFactory(
    private val sessionGraph: SessionGraph,
) : RoomGraphFactory {
    override fun create(room: JoinedRoom): Any {
        return sessionGraph.roomGraphFactory
            .create(room, room)
    }
}
