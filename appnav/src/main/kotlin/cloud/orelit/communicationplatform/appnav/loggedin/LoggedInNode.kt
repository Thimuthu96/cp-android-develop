/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.loggedin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class LoggedInNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val loggedInPresenter: LoggedInPresenter,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun navigateToNotificationTroubleshoot()
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val loggedInState = loggedInPresenter.present()
        LoggedInView(
            state = loggedInState,
            navigateToNotificationTroubleshoot = callback::navigateToNotificationTroubleshoot,
            modifier = modifier
        )
    }
}
