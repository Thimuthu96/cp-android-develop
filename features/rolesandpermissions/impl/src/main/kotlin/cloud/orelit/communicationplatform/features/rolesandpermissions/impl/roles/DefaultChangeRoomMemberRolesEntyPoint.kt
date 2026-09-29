/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rolesandpermissions.impl.roles

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom

@ContributesBinding(SessionScope::class)
class DefaultChangeRoomMemberRolesEntyPoint : ChangeRoomMemberRolesEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        room: JoinedRoom,
        listType: ChangeRoomMemberRolesListType,
    ): Node {
        return parentNode.createNode<ChangeRoomMemberRolesRootNode>(
            buildContext = buildContext,
            plugins = listOf(
                ChangeRoomMemberRolesRootNode.Inputs(joinedRoom = room, listType = listType),
            )
        )
    }
}
