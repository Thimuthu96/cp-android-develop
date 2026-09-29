/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.impl.root

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
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkBuilder
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesNode(SessionScope::class)
@AssistedInject
class UserProfileNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val analyticsService: AnalyticsService,
    private val permalinkBuilder: PermalinkBuilder,
    presenterFactory: UserProfilePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class UserProfileInputs(
        val userId: UserId
    ) : NodeInputs

    private val inputs = inputs<UserProfileInputs>()
    private val callback = inputs<UserProfileNodeHelper.Callback>()
    private val presenter = presenterFactory.create(userId = inputs.userId)
    private val userProfileNodeHelper = UserProfileNodeHelper(inputs.userId)

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

        fun onStartDM(roomId: RoomId) {
            callback.navigateToRoom(roomId)
        }

        val state = presenter.present()

        UserProfileView(
            state = state,
            modifier = modifier,
            goBack = this::navigateUp,
            onShareUser = ::onShareUser,
            onOpenDm = ::onStartDM,
            onStartCall = callback::startCall,
            openAvatarPreview = callback::navigateToAvatarPreview,
            onVerifyClick = callback::startVerifyUserFlow,
        )
    }
}
