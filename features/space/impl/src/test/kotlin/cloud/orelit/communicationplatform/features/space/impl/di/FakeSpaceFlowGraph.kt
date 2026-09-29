/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.di

import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.architecture.AssistedNodeFactory
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoomList
import kotlin.reflect.KClass

class FakeSpaceFlowGraph : SpaceFlowGraph {
    object Factory : SpaceFlowGraph.Factory {
        override fun create(spaceRoomList: SpaceRoomList): SpaceFlowGraph {
            return FakeSpaceFlowGraph()
        }
    }

    override fun nodeFactories(): Map<KClass<out Node>, AssistedNodeFactory<*>> {
        return emptyMap()
    }
}
