/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cloud.orelit.communicationplatform.features.space.impl

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.createroom.api.CreateRoomEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import cloud.orelit.communicationplatform.features.space.api.SpaceEntryPoint
import cloud.orelit.communicationplatform.features.space.impl.addroom.AddRoomToSpaceNode
import cloud.orelit.communicationplatform.features.space.impl.di.SpaceFlowGraph
import cloud.orelit.communicationplatform.features.space.impl.leave.LeaveSpaceNode
import cloud.orelit.communicationplatform.features.space.impl.root.SpaceNode
import cloud.orelit.communicationplatform.features.space.impl.settings.SpaceSettingsFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.DependencyInjectionGraphOwner
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceService
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.loadAllIncrementally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class SpaceFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: JoinedRoom,
    spaceService: SpaceService,
    graphFactory: SpaceFlowGraph.Factory,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val changeRoomMemberRolesEntryPoint: ChangeRoomMemberRolesEntryPoint,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : BaseFlowNode<SpaceFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
), DependencyInjectionGraphOwner {
    private val callback: SpaceEntryPoint.Callback = callback()
    private val spaceRoomList = spaceService.spaceRoomList(room.roomId)
    override val graph = graphFactory.create(spaceRoomList)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class Settings(val initialTarget: SpaceSettingsFlowNode.NavTarget = SpaceSettingsFlowNode.NavTarget.Root) : NavTarget

        @Parcelize
        data object Leave : NavTarget

        @Parcelize
        data object CreateRoom : NavTarget

        @Parcelize
        data object AddRoom : NavTarget

        @Parcelize
        data object ChangeOwners : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                spaceRoomList.loadAllIncrementally(lifecycleScope)
            },
            onDestroy = {
                spaceRoomList.destroy()
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Leave -> {
                val callback = object : LeaveSpaceNode.Callback {
                    override fun closeLeaveSpaceFlow() {
                        backstack.pop()
                    }

                    override fun navigateToRolesAndPermissions() {
                        backstack.push(NavTarget.Settings(SpaceSettingsFlowNode.NavTarget.RolesAndPermissions))
                    }

                    override fun navigateToChooseOwners() {
                        backstack.replace(NavTarget.ChangeOwners)
                    }
                }
                createNode<LeaveSpaceNode>(buildContext, listOf(callback))
            }
            NavTarget.Root -> {
                val callback = object : SpaceNode.Callback {
                    override fun navigateToRoom(roomId: RoomId, viaParameters: List<String>) {
                        callback.navigateToRoom(roomId, viaParameters)
                    }

                    override fun navigateToSpaceSettings() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun navigateToRoomMemberList() {
                        callback.navigateToRoomMemberList()
                    }

                    override fun startLeaveSpaceFlow() {
                        backstack.push(NavTarget.Leave)
                    }

                    override fun onCreateRoom() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun navigateToAddRoom() {
                        backstack.push(NavTarget.AddRoom)
                    }
                }
                createNode<SpaceNode>(buildContext, listOf(callback))
            }
            is NavTarget.Settings -> {
                val callback = object : SpaceSettingsFlowNode.Callback {
                    override fun initialTarget() = navTarget.initialTarget

                    override fun navigateToSpaceMembers() {
                        callback.navigateToRoomMemberList()
                    }

                    override fun startLeaveSpaceFlow() {
                        backstack.push(NavTarget.Leave)
                    }

                    override fun closeSettings() {
                        backstack.pop()
                    }
                }
                createNode<SpaceSettingsFlowNode>(buildContext, listOf(callback))
            }
            is NavTarget.CreateRoom -> {
                val callback = object : CreateRoomEntryPoint.Callback {
                    override fun onRoomCreated(roomId: RoomId) {
                        // Reset the room list in the space so this new room is displayed
                        lifecycleScope.launch { spaceRoomList.reset() }
                        callback.navigateToRoom(roomId, emptyList())
                        backstack.pop()
                    }
                }
                createRoomEntryPoint
                    .builder(
                        parentNode = this,
                        buildContext = buildContext,
                        callback = callback,
                    )
                    .setParentSpace(spaceRoomList.spaceId)
                    .build()
            }
            NavTarget.AddRoom -> {
                val callback = object : AddRoomToSpaceNode.Callback {
                    override fun onFinish() {
                        backstack.pop()
                    }
                }
                createNode<AddRoomToSpaceNode>(buildContext, listOf(callback))
            }
            NavTarget.ChangeOwners -> {
                val node = changeRoomMemberRolesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    room = room,
                    listType = ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving,
                )

                val completionProxy = node as ChangeRoomMemberRolesEntryPoint.NodeProxy
                sessionCoroutineScope.launch {
                    val changedOwners = withContext(NonCancellable) {
                        completionProxy.waitForCompletion()
                    }

                    if (changedOwners) {
                        backstack.replace(NavTarget.Leave)
                    } else {
                        backstack.pop()
                    }
                }

                node
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) = BackstackView()
}
