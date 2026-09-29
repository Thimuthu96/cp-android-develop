/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomRenderer
import cloud.orelit.communicationplatform.libraries.androidutils.system.startSharePlainTextIntent
import cloud.orelit.communicationplatform.libraries.architecture.appyx.launchMolecule
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.room.BaseRoom
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import cloud.orelit.communicationplatform.libraries.androidutils.R as AndroidUtilsR

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomDetailsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: RoomDetailsPresenter.Factory,
    private val room: BaseRoom,
    private val analyticsService: AnalyticsService,
    private val leaveRoomRenderer: LeaveRoomRenderer,
) : Node(buildContext, plugins = plugins), RoomDetailsNavigator {
    interface Callback : Plugin {
        fun navigateBack()
        fun navigateToRoomMemberList()
        fun navigateToInviteMembers()
        fun navigateToRoomDetailsEdit()
        fun navigateToRoomNotificationSettings()
        fun navigateToAvatarPreview(name: String, url: String)
        fun navigateToPollHistory()
        fun navigateToMediaGallery()
        fun navigateToAdminSettings()
        fun navigateToPinnedMessagesList()
        fun navigateToKnockRequestsList()
        fun navigateToSecurityAndPrivacy()
        fun navigateToRoomMemberDetails(userId: UserId)
        fun navigateToRoomCall(callIntent: CallIntent)
        fun navigateToReportRoom()
        fun navigateToSelectNewOwnersWhenLeaving()
    }

    private val presenter = presenterFactory.create(this)
    private val callback: Callback = callback()

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomDetails))
            }
        )
    }

    private fun CoroutineScope.onShareRoom(context: Context) = launch {
        room.getPermalink()
            .onSuccess { permalink ->
                context.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = context.getString(R.string.screen_room_details_share_room_title),
                    text = permalink,
                    noActivityFoundMessage = context.getString(AndroidUtilsR.string.error_no_compatible_app_found)
                )
            }
            .onFailure {
                Timber.e(it)
            }
    }

    private val stateFlow = launchMolecule { presenter.present() }

    fun onNewOwnersSelected() {
        stateFlow.value.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = false))
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current
        val state by stateFlow.collectAsState()

        fun onShareRoom() {
            lifecycleScope.onShareRoom(context)
        }

        fun onActionClick(action: RoomDetailsAction) {
            when (action) {
                RoomDetailsAction.Edit -> {
                    callback.navigateToRoomDetailsEdit()
                }
                RoomDetailsAction.AddTopic -> {
                    callback.navigateToRoomDetailsEdit()
                }
            }
        }

        RoomDetailsView(
            state = state,
            modifier = modifier,
            goBack = ::navigateUp,
            onActionClick = ::onActionClick,
            onShareRoom = ::onShareRoom,
            openRoomMemberList = callback::navigateToRoomMemberList,
            openRoomNotificationSettings = callback::navigateToRoomNotificationSettings,
            invitePeople = callback::navigateToInviteMembers,
            openAvatarPreview = callback::navigateToAvatarPreview,
            openPollHistory = callback::navigateToPollHistory,
            openMediaGallery = callback::navigateToMediaGallery,
            openAdminSettings = callback::navigateToAdminSettings,
            onJoinCallClick = callback::navigateToRoomCall,
            onPinnedMessagesClick = callback::navigateToPinnedMessagesList,
            onKnockRequestsClick = callback::navigateToKnockRequestsList,
            onSecurityAndPrivacyClick = callback::navigateToSecurityAndPrivacy,
            onProfileClick = callback::navigateToRoomMemberDetails,
            onReportRoomClick = callback::navigateToReportRoom,
            leaveRoomView = {
                leaveRoomRenderer.Render(
                    state = state.leaveRoomState,
                    onSelectNewOwners = { callback.navigateToSelectNewOwnersWhenLeaving() },
                    modifier = Modifier
                )
            }
        )
    }

    override fun onDone() {
        callback.navigateBack()
    }
}
