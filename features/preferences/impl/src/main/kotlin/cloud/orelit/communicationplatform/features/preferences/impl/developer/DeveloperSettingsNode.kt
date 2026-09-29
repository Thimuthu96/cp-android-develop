/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.developer

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.airbnb.android.showkase.models.Showkase
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.preferences.api.ExtraDeveloperOptionsRenderer
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.designsystem.showkase.getBrowserIntent
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class DeveloperSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: DeveloperSettingsPresenter.Factory,
    private val extraDeveloperOptionsRenderer: ExtraDeveloperOptionsRenderer,
) : Node(buildContext, plugins = plugins),
    DeveloperSettingsNavigator {
    interface Callback : Plugin {
        fun navigateToPushRules(filename: String, content: String)
        fun navigateToPushHistory()
        fun onDone()
    }

    private val callback: Callback = callback()
    private val presenter = presenterFactory.create(navigator = this)

    override fun openPushRules(filename: String, content: String) {
        callback.navigateToPushRules(filename = filename, content = content)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        fun openShowkase() {
            val intent = Showkase.getBrowserIntent(activity)
            activity.startActivity(intent)
        }

        val state = presenter.present()
        DeveloperSettingsView(
            state = state,
            modifier = modifier,
            onOpenShowkase = ::openShowkase,
            onPushHistoryClick = callback::navigateToPushHistory,
            onBackClick = callback::onDone,
            extraOptions = { extraDeveloperOptionsRenderer.Render(Modifier) },
        )
    }
}
