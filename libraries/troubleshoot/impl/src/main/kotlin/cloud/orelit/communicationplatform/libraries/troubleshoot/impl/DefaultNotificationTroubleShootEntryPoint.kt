/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint

@ContributesBinding(AppScope::class)
class DefaultNotificationTroubleShootEntryPoint : NotificationTroubleShootEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: NotificationTroubleShootEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<TroubleshootNotificationsNode>(buildContext, listOf(callback))
    }
}
