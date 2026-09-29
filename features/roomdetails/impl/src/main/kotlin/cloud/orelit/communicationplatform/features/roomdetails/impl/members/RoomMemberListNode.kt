/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationAction
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationEvent
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationRenderer
import cloud.orelit.communicationplatform.libraries.architecture.appyx.launchMolecule
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomMemberListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomMemberListPresenter,
    private val analyticsService: AnalyticsService,
    private val roomMemberModerationRenderer: RoomMemberModerationRenderer,
) : Node(buildContext, plugins = plugins), RoomMemberListNavigator {
    interface Callback : Plugin {
        fun navigateToRoomMemberDetails(roomMemberId: UserId)
        fun navigateToInviteMembers()
        fun navigateToAvatarPreview(username: String, avatarUrl: String)
    }

    private val callback: Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomMembers))
            }
        )
    }

    override fun openRoomMemberDetails(roomMemberId: UserId) {
        callback.navigateToRoomMemberDetails(roomMemberId)
    }

    override fun openInviteMembers() {
        callback.navigateToInviteMembers()
    }

    override fun exitRoomMemberList() {
        navigateUp()
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        RoomMemberListView(
            state = state,
            modifier = modifier,
            navigator = this,
        )
        roomMemberModerationRenderer.Render(
            state = state.moderationState,
            onSelectAction = { action, target ->
                when (action) {
                    is ModerationAction.DisplayProfile -> openRoomMemberDetails(target.userId)
                    else -> state.moderationState.eventSink(RoomMemberModerationEvent.ProcessAction(action, target))
                }
            },
            onAvatarClick = { user ->
                user.avatarUrl?.let { url ->
                    callback.navigateToAvatarPreview(user.getBestName(), url)
                }
            },
            modifier = Modifier,
        )
    }
}

interface RoomMemberListNavigator {
    fun exitRoomMemberList()
    fun openRoomMemberDetails(roomMemberId: UserId)
    fun openInviteMembers()
}
