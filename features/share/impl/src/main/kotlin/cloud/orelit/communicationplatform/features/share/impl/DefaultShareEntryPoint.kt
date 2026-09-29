/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.share.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.share.api.ShareEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultShareEntryPoint : ShareEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: ShareEntryPoint.Params,
        callback: ShareEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<ShareNode>(
            buildContext = buildContext,
            plugins = listOf(
                ShareNode.Inputs(shareIntentData = params.shareIntentData),
                callback,
            )
        )
    }
}
