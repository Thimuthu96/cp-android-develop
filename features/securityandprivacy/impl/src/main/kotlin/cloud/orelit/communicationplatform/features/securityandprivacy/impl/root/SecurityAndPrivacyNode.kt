/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securityandprivacy.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import cloud.orelit.communicationplatform.libraries.androidutils.browser.openUrlInChromeCustomTab
import cloud.orelit.communicationplatform.libraries.architecture.appyx.launchMolecule
import cloud.orelit.communicationplatform.libraries.di.RoomScope

@ContributesNode(RoomScope::class)
@AssistedInject
class SecurityAndPrivacyNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SecurityAndPrivacyPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val navigator = plugins<SecurityAndPrivacyNavigator>().first()
    private val presenter = presenterFactory.create(navigator)

    private val stateFlow = launchMolecule { presenter.present() }

    private fun onOpenExternalUrl(activity: Activity, darkTheme: Boolean, url: String) {
        activity.openUrlInChromeCustomTab(null, darkTheme, url)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        val state by stateFlow.collectAsState()
        SecurityAndPrivacyView(
            state = state,
            onLinkClick = { url ->
                onOpenExternalUrl(activity, isDark, url)
            },
            modifier = modifier
        )
    }
}
