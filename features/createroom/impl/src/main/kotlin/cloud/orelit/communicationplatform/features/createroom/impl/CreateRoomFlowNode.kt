/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.createroom.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.replace
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.createroom.api.CreateRoomEntryPoint
import cloud.orelit.communicationplatform.features.createroom.impl.addpeople.AddPeopleNode
import cloud.orelit.communicationplatform.features.createroom.impl.configureroom.ConfigureRoomNode
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class CreateRoomFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<CreateRoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = initialElementFromInputs(plugins.filterIsInstance<Inputs>().first()),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    @Parcelize
    data class Inputs(
        val isSpace: Boolean,
        val parentSpaceId: RoomId?,
    ) : NodeInputs, Parcelable

    private val callback: CreateRoomEntryPoint.Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.ConfigureRoom -> {
                val inputs = ConfigureRoomNode.Inputs(isSpace = navTarget.isSpace, parentSpaceId = navTarget.parentSpaceId)
                val callback = object : ConfigureRoomNode.Callback {
                    override fun onCreateRoomSuccess(roomId: RoomId) {
                        backstack.replace(NavTarget.AddPeople(roomId))
                    }
                }
                createNode<ConfigureRoomNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.AddPeople -> {
                val inputs = AddPeopleNode.Inputs(navTarget.roomId)
                val callback: AddPeopleNode.Callback = object : AddPeopleNode.Callback {
                    override fun onFinish() {
                        callback.onRoomCreated(navTarget.roomId)
                    }
                }
                createNode<AddPeopleNode>(buildContext, plugins = listOf(inputs, callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data class ConfigureRoom(val isSpace: Boolean, val parentSpaceId: RoomId?) : NavTarget

        @Parcelize
        data class AddPeople(val roomId: RoomId) : NavTarget
    }
}

private fun initialElementFromInputs(inputs: CreateRoomFlowNode.Inputs) = CreateRoomFlowNode.NavTarget.ConfigureRoom(
    isSpace = inputs.isSpace,
    parentSpaceId = inputs.parentSpaceId,
)
