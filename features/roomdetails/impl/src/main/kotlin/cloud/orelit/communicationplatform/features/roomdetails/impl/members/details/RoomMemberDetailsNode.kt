/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.userprofile.shared.UserProfileNodeHelper
import cloud.orelit.communicationplatform.features.userprofile.shared.UserProfileView
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkBuilder
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomMemberDetailsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val analyticsService: AnalyticsService,
    private val permalinkBuilder: PermalinkBuilder,
    presenterFactory: RoomMemberDetailsPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class RoomMemberDetailsInput(
        val roomMemberId: UserId,
    ) : NodeInputs

    private val inputs = inputs<RoomMemberDetailsInput>()
    private val callback = inputs<UserProfileNodeHelper.Callback>()
    private val presenter = presenterFactory.create(inputs.roomMemberId)
    private val userProfileNodeHelper = UserProfileNodeHelper(inputs.roomMemberId)

    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.User))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val context = LocalContext.current

        fun onShareUser() {
            userProfileNodeHelper.onShareUser(context, permalinkBuilder)
        }

        fun navigateToRoom(roomId: RoomId) {
            callback.navigateToRoom(roomId)
        }

        fun onStartCall(roomId: RoomId, callIntent: CallIntent) {
            callback.startCall(roomId, callIntent)
        }

        val state = presenter.present()

        UserProfileView(
            state = state,
            modifier = modifier,
            goBack = this::navigateUp,
            onShareUser = ::onShareUser,
            onOpenDm = ::navigateToRoom,
            onStartCall = ::onStartCall,
            openAvatarPreview = callback::navigateToAvatarPreview,
            onVerifyClick = callback::startVerifyUserFlow,
        )
    }
}
