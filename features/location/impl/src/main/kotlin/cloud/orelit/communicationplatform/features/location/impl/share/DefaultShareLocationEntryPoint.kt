/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.share

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.location.api.ShareLocationEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline

@ContributesBinding(AppScope::class)
class DefaultShareLocationEntryPoint : ShareLocationEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        timelineMode: Timeline.Mode,
    ): Node {
        return parentNode.createNode<ShareLocationNode>(
            buildContext = buildContext,
            plugins = listOf(ShareLocationNode.Inputs(timelineMode))
        )
    }
}
