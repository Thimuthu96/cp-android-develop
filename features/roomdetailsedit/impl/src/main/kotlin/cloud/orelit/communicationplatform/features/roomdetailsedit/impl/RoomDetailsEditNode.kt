/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetailsedit.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.MobileScreen
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomDetailsEditNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RoomDetailsEditPresenter,
    private val analyticsService: AnalyticsService,
) : Node(buildContext, plugins = plugins) {
    init {
        lifecycle.subscribe(
            onResume = {
                analyticsService.screen(MobileScreen(screenName = MobileScreen.ScreenName.RoomSettings))
            }
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RoomDetailsEditView(
            state = state,
            onDone = ::navigateUp,
            modifier = modifier,
        )
    }
}
