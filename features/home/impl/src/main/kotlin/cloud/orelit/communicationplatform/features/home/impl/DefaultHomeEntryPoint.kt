/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import cloud.orelit.communicationplatform.features.home.api.HomeEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class DefaultHomeEntryPoint : HomeEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: HomeEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<HomeFlowNode>(buildContext, listOf(callback))
    }
}
