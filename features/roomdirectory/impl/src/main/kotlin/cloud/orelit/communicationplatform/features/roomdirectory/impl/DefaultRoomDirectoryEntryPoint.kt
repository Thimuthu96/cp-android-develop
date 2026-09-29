/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdirectory.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDirectoryEntryPoint
import cloud.orelit.communicationplatform.features.roomdirectory.impl.root.RoomDirectoryNode
import cloud.orelit.communicationplatform.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultRoomDirectoryEntryPoint : RoomDirectoryEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: RoomDirectoryEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<RoomDirectoryNode>(buildContext, listOf(callback))
    }
}
