/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x.di

import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.appnav.di.TimelineBindings
import cloud.orelit.communicationplatform.libraries.architecture.NodeFactoriesBindings
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.room.BaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom

@GraphExtension(RoomScope::class)
interface RoomGraph : NodeFactoriesBindings, TimelineBindings {
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides joinedRoom: JoinedRoom,
            @Provides baseRoom: BaseRoom
        ): RoomGraph
    }
}
