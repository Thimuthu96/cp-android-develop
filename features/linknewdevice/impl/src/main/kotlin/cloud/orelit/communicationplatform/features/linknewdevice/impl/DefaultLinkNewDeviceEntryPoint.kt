/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.linknewdevice.api.LinkNewDeviceEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultLinkNewDeviceEntryPoint : LinkNewDeviceEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: LinkNewDeviceEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<LinkNewDeviceFlowNode>(
            buildContext = buildContext,
            plugins = listOf(
                callback,
            )
        )
    }
}
