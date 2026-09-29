/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.leave

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.space.impl.di.SpaceFlowScope
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class LeaveSpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    matrixClient: MatrixClient,
    room: JoinedRoom,
    presenterFactory: LeaveSpacePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun closeLeaveSpaceFlow()
        fun navigateToRolesAndPermissions()
        fun navigateToChooseOwners()
    }

    private val leaveSpaceHandle = matrixClient.spaceService.getLeaveSpaceHandle(room.roomId)
    private val presenter: LeaveSpacePresenter = presenterFactory.create(leaveSpaceHandle)

    private val callback: Callback = callback()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onDestroy = {
                leaveSpaceHandle.close()
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LeaveSpaceView(
            state = state,
            onCancel = callback::closeLeaveSpaceFlow,
            onRolesAndPermissionsClick = callback::navigateToRolesAndPermissions,
            onChooseOwnersClick = callback::navigateToChooseOwners,
            modifier = modifier
        )
    }
}
