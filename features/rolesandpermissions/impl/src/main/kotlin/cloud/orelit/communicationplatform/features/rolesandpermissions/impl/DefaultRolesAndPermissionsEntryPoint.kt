/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rolesandpermissions.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.RolesAndPermissionsEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.RoomScope

@ContributesBinding(RoomScope::class)
class DefaultRolesAndPermissionsEntryPoint : RolesAndPermissionsEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: RolesAndPermissionsEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<RolesAndPermissionsFlowNode>(buildContext, listOf(callback))
    }
}
