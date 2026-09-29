/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rolesandpermissions.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.RolesAndPermissionsEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.impl.permissions.ChangeRoomPermissionsNode
import cloud.orelit.communicationplatform.features.rolesandpermissions.impl.roles.ChangeRolesNode
import cloud.orelit.communicationplatform.features.rolesandpermissions.impl.root.RolesAndPermissionsNode
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncIndicatorHost
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncIndicatorState
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.canEditRolesAndPermissions
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.permissionsFlow
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class RolesAndPermissionsFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: JoinedRoom,
) : BaseFlowNode<RolesAndPermissionsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object ChangeAdmins : NavTarget

        @Parcelize
        data object ChangeModerators : NavTarget

        @Parcelize
        data object ChangeRoomPermissions : NavTarget
    }

    private val callback: RolesAndPermissionsEntryPoint.Callback = callback()
    private val asyncIndicatorState = AsyncIndicatorState()

    override fun onBuilt() {
        super.onBuilt()
        whenChildAttached { lifecycle, node: ChangeRolesNode ->
            lifecycle.coroutineScope.launch {
                val changesSaved = node.waitForCompletion()
                onChangeComplete(changesSaved)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                room.permissionsFlow(false) { perms -> perms.canEditRolesAndPermissions() }
                    .filter { canEdit -> !canEdit }
                    .first()
                // If the user can no longer edit roles and permissions, exit the flow
                callback.onDone()
            }
        }
    }

    private fun onChangeComplete(changesSaved: Boolean) {
        backstack.pop()
        if (changesSaved) {
            asyncIndicatorState.enqueue(durationMs = AsyncIndicator.DURATION_SHORT) {
                AsyncIndicator.Custom(text = stringResource(CommonStrings.common_saved_changes))
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : RolesAndPermissionsNode.Callback {
                    override fun openAdminList() {
                        backstack.push(NavTarget.ChangeAdmins)
                    }

                    override fun openModeratorList() {
                        backstack.push(NavTarget.ChangeModerators)
                    }

                    override fun openEditPermissions() {
                        backstack.push(NavTarget.ChangeRoomPermissions)
                    }
                }
                createNode<RolesAndPermissionsNode>(
                    buildContext = buildContext,
                    plugins = listOf(callback),
                )
            }
            is NavTarget.ChangeAdmins -> {
                val inputs = ChangeRolesNode.Inputs(ChangeRoomMemberRolesListType.Admins)
                createNode<ChangeRolesNode>(buildContext = buildContext, plugins = listOf(inputs))
            }
            is NavTarget.ChangeModerators -> {
                val inputs = ChangeRolesNode.Inputs(ChangeRoomMemberRolesListType.Moderators)
                createNode<ChangeRolesNode>(buildContext = buildContext, plugins = listOf(inputs))
            }
            is NavTarget.ChangeRoomPermissions -> {
                val callback = object : ChangeRoomPermissionsNode.Callback {
                    override fun onComplete(changesSaved: Boolean) {
                        onChangeComplete(changesSaved)
                    }
                }
                createNode<ChangeRoomPermissionsNode>(buildContext = buildContext, plugins = listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            BackstackView()
            AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), asyncIndicatorState)
        }
    }
}
