/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutEvent
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutView
import cloud.orelit.communicationplatform.libraries.androidutils.browser.openUrlInChromeCustomTab
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.emoji.api.picker.EmojiPickerRenderer
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PreferencesRootPresenter,
    private val directLogoutView: DirectLogoutView,
    private val emojiPickerRenderer: EmojiPickerRenderer,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToAddAccount()
        fun navigateToBugReport()
        fun navigateToSecureBackup()
        fun navigateToAnalyticsSettings()
        fun navigateToAbout()
        fun navigateToDeveloperSettings()
        fun navigateToNotificationSettings()
        fun navigateToLockScreenSettings()
        fun navigateToAdvancedSettings()
        fun navigateToLabs()
        fun navigateToLinkNewDevice()
        fun navigateToUserProfile(matrixUser: MatrixUser)
        fun navigateToBlockedUsers()
        fun startSignOutFlow()
        fun startAccountDeactivationFlow()
    }

    private val callback: Callback = callback()

    private fun onManageAccountClick(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        PreferencesRootView(
            state = state,
            emojiPickerRenderer = emojiPickerRenderer,
            modifier = modifier,
            onBackClick = this::navigateUp,
            onAddAccountClick = callback::navigateToAddAccount,
            onOpenRageShake = callback::navigateToBugReport,
            onOpenAnalytics = callback::navigateToAnalyticsSettings,
            onOpenAbout = callback::navigateToAbout,
            onSecureBackupClick = callback::navigateToSecureBackup,
            onOpenDeveloperSettings = callback::navigateToDeveloperSettings,
            onOpenAdvancedSettings = callback::navigateToAdvancedSettings,
            onOpenLabs = callback::navigateToLabs,
            onLinkNewDeviceClick = callback::navigateToLinkNewDevice,
            onManageAccountClick = { onManageAccountClick(activity, it, isDark) },
            onOpenNotificationSettings = callback::navigateToNotificationSettings,
            onOpenLockScreenSettings = callback::navigateToLockScreenSettings,
            onOpenUserProfile = callback::navigateToUserProfile,
            onOpenBlockedUsers = callback::navigateToBlockedUsers,
            onSignOutClick = {
                if (state.directLogoutState.canDoDirectSignOut) {
                    state.directLogoutState.eventSink(DirectLogoutEvent.Logout(ignoreSdkError = false))
                } else {
                    callback.startSignOutFlow()
                }
            },
            onDeactivateClick = callback::startAccountDeactivationFlow
        )

        directLogoutView.Render(state = state.directLogoutState)
    }
}
