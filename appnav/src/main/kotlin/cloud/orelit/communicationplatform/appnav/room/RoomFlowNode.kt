/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.room

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.transition.JumpToEndTransitionHandler
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.active
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.appnav.room.joined.JoinedRoomFlowNode
import cloud.orelit.communicationplatform.appnav.room.joined.JoinedRoomLoadedFlowNode
import cloud.orelit.communicationplatform.appnav.room.joined.LoadingRoomNodeView
import cloud.orelit.communicationplatform.features.joinroom.api.JoinRoomEntryPoint
import cloud.orelit.communicationplatform.features.roomaliasesolver.api.RoomAliasResolverEntryPoint
import cloud.orelit.communicationplatform.features.roomaliasesolver.api.RoomAliasResolverEntryPoint.Params
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.core.coroutine.withPreviousValue
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipObserver
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.LoadingRoomState
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.LoadJoinedRoomFlow
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.NotificationToMessage
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.OpenRoom
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import im.vector.app.features.analytics.plan.JoinedRoom as JoinedRoomAnalyticsEvent
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom as JoinedRoomInstance

@ContributesNode(SessionScope::class)
@AssistedInject
class RoomFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val client: MatrixClient,
    private val joinRoomEntryPoint: JoinRoomEntryPoint,
    private val roomAliasResolverEntryPoint: RoomAliasResolverEntryPoint,
    private val membershipObserver: RoomMembershipObserver,
    private val analyticsService: AnalyticsService,
) : BaseFlowNode<RoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = run {
            val joinedRoom = (plugins.filterIsInstance<Inputs>().first().initialElement as? RoomNavigationTarget.Root)?.joinedRoom
            if (joinedRoom != null) {
                NavTarget.JoinedRoom(joinedRoom)
            } else {
                NavTarget.Loading
            }
        },
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    data class Inputs(
        val roomIdOrAlias: RoomIdOrAlias,
        val roomDescription: Optional<RoomDescription>,
        val serverNames: List<String>,
        val trigger: Optional<JoinedRoomAnalyticsEvent.Trigger>,
        val initialElement: RoomNavigationTarget,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data class Resolving(val roomAlias: RoomAlias) : NavTarget

        @Parcelize
        data class JoinRoom(
            val roomId: RoomId,
            val serverNames: List<String>,
            val trigger: JoinedRoomAnalyticsEvent.Trigger,
        ) : NavTarget

        @Parcelize
        data class JoinedRoom(
            val roomId: RoomId,
            @IgnoredOnParcel val joinedRoom: JoinedRoomInstance? = null,
        ) : NavTarget {
            constructor(joinedRoom: JoinedRoomInstance) : this(joinedRoom.roomId, joinedRoom)
        }
    }

    override fun onBuilt() {
        super.onBuilt()
        val parentTransaction = analyticsService.getLongRunningTransaction(NotificationToMessage)
        val openRoomTransaction = analyticsService.startLongRunningTransaction(OpenRoom, parentTransaction)
        analyticsService.startLongRunningTransaction(LoadJoinedRoomFlow, openRoomTransaction)
        resolveRoomId()
    }

    private fun resolveRoomId() {
        lifecycleScope.launch {
            when (val i = inputs.roomIdOrAlias) {
                is RoomIdOrAlias.Alias -> {
                    backstack.newRoot(NavTarget.Resolving(i.roomAlias))
                }
                is RoomIdOrAlias.Id -> {
                    subscribeToRoomInfoFlow(i.roomId, inputs.serverNames)
                }
            }
        }
    }

    private fun subscribeToRoomInfoFlow(roomId: RoomId, serverNames: List<String>) {
        val joinedRoom = (inputs.initialElement as? RoomNavigationTarget.Root)?.joinedRoom
        val roomInfoFlow = joinedRoom?.roomInfoFlow?.map { Optional.of(it) }
            ?: client.getRoomInfoFlow(roomId)

        // This observes the local membership changes for the room
        val membershipUpdateFlow = membershipObserver.updates
            .filter { it.roomId == roomId }
            .distinctUntilChanged()
            // We add a replay so we can check the last local membership update
            .shareIn(lifecycleScope, started = SharingStarted.Eagerly, replay = 1)

        val currentMembershipFlow = roomInfoFlow
            .map { it.getOrNull()?.currentUserMembership }
            .distinctUntilChanged()
            .withPreviousValue()
        currentMembershipFlow.onEach { (previousMembership, membership) ->
            Timber.d("Room membership: $membership")
            if (membership == CurrentUserMembership.JOINED) {
                val currentNavTarget = backstack.active?.key?.navTarget
                if (currentNavTarget is NavTarget.JoinedRoom && currentNavTarget.roomId == roomId) {
                    Timber.d("Already in JoinedRoom $roomId, do nothing")
                    return@onEach
                }
                backstack.newRoot(NavTarget.JoinedRoom(roomId))
            } else {
                val leavingFromCurrentDevice =
                    membership == CurrentUserMembership.LEFT &&
                        previousMembership == CurrentUserMembership.JOINED &&
                        membershipUpdateFlow.replayCache.lastOrNull()?.isUserInRoom == false

                if (leavingFromCurrentDevice) {
                    navigateUp()
                } else {
                    backstack.newRoot(
                        NavTarget.JoinRoom(
                            roomId = roomId,
                            serverNames = serverNames,
                            trigger = inputs.trigger.getOrNull() ?: JoinedRoomAnalyticsEvent.Trigger.Invite,
                        )
                    )
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Loading -> loadingNode(buildContext)
            is NavTarget.Resolving -> {
                val callback = object : RoomAliasResolverEntryPoint.Callback {
                    override fun onAliasResolved(data: ResolvedRoomAlias) {
                        subscribeToRoomInfoFlow(
                            roomId = data.roomId,
                            serverNames = data.servers,
                        )
                    }
                }
                val params = Params(navTarget.roomAlias)
                roomAliasResolverEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            is NavTarget.JoinRoom -> {
                // Clear analytics transactions for opening a joined room, since we're display a non-joined one
                analyticsService.removeLongRunningTransaction(LoadJoinedRoomFlow)
                analyticsService.removeLongRunningTransaction(OpenRoom)

                val inputs = JoinRoomEntryPoint.Inputs(
                    roomId = navTarget.roomId,
                    roomIdOrAlias = inputs.roomIdOrAlias,
                    roomDescription = inputs.roomDescription,
                    serverNames = navTarget.serverNames,
                    trigger = navTarget.trigger,
                )
                joinRoomEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    inputs = inputs,
                )
            }
            is NavTarget.JoinedRoom -> {
                val roomFlowNodeCallback = plugins<JoinedRoomLoadedFlowNode.Callback>()
                val inputs = JoinedRoomFlowNode.Inputs(
                    roomId = navTarget.roomId,
                    initialElement = inputs.initialElement,
                    joinedRoom = navTarget.joinedRoom,
                )
                createNode<JoinedRoomFlowNode>(buildContext, plugins = listOf(inputs) + roomFlowNodeCallback)
            }
        }
    }

    suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?) {
        waitForChildAttached<JoinedRoomFlowNode>()
            .attachThread(threadId, focusedEventId)
    }

    private fun loadingNode(buildContext: BuildContext) = node(buildContext) { modifier ->
        LoadingRoomNodeView(
            state = LoadingRoomState.Loading,
            onBackClick = { navigateUp() },
            modifier = modifier,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(transitionHandler = JumpToEndTransitionHandler())
    }
}
