/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cloud.orelit.communicationplatform.appnav.room.joined

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.appnav.room.RoomNavigationTarget
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.LoadingRoomState
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.LoadingRoomStateFlowFactory
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.LoadJoinedRoomFlow
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.NotificationToMessage
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.OpenRoom
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class JoinedRoomFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    loadingRoomStateFlowFactory: LoadingRoomStateFlowFactory,
    private val analyticsService: AnalyticsService,
) :
    BaseFlowNode<JoinedRoomFlowNode.NavTarget>(
        backstack = BackStack(
            initialElement = NavTarget.Loading,
            savedStateMap = buildContext.savedStateMap,
        ),
        buildContext = buildContext,
        plugins = plugins
    ) {
    data class Inputs(
        val roomId: RoomId,
        val joinedRoom: JoinedRoom?,
        val initialElement: RoomNavigationTarget,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val loadingRoomStateStateFlow = loadingRoomStateFlowFactory.create(lifecycleScope, inputs.roomId, inputs.joinedRoom)

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data object Loaded : NavTarget
    }

    override fun onBuilt() {
        super.onBuilt()

        val parentTransaction = analyticsService.getLongRunningTransaction(NotificationToMessage)
        val openRoomTransaction = analyticsService.startLongRunningTransaction(OpenRoom, parentTransaction)
        analyticsService.startLongRunningTransaction(LoadJoinedRoomFlow, openRoomTransaction)

        loadingRoomStateStateFlow
            .map {
                it is LoadingRoomState.Loaded
            }
            .distinctUntilChanged()
            .onEach { isLoaded ->
                if (isLoaded) {
                    backstack.newRoot(NavTarget.Loaded)
                } else {
                    backstack.newRoot(NavTarget.Loading)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loaded -> {
                val roomFlowNodeCallback = plugins<JoinedRoomLoadedFlowNode.Callback>()
                val awaitRoomState = loadingRoomStateStateFlow.value
                if (awaitRoomState is LoadingRoomState.Loaded) {
                    val inputs = JoinedRoomLoadedFlowNode.Inputs(
                        room = awaitRoomState.room,
                        initialElement = inputs.initialElement
                    )
                    createNode<JoinedRoomLoadedFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
                } else {
                    loadingNode(buildContext, this::navigateUp)
                }
            }
            NavTarget.Loading -> {
                loadingNode(buildContext, this::navigateUp)
            }
        }
    }

    private fun loadingNode(buildContext: BuildContext, onBackClick: () -> Unit) = node(buildContext) { modifier ->
        val loadingRoomState by loadingRoomStateStateFlow.collectAsState()
        LoadingRoomNodeView(
            state = loadingRoomState,
            onBackClick = onBackClick,
            modifier = modifier
        )
    }

    suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?) {
        waitForChildAttached<JoinedRoomLoadedFlowNode>()
            .attachThread(threadId, focusedEventId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(
            transitionHandler = JumpToEndTransitionHandler(),
        )
    }
}
