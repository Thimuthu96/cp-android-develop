/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.permissions.impl.troubleshoot

import android.Manifest
import android.os.Build
import dev.zacsweers.metro.ContributesIntoSet
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionStateProvider
import cloud.orelit.communicationplatform.libraries.permissions.impl.R
import cloud.orelit.communicationplatform.libraries.permissions.impl.action.PermissionActions
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import cloud.orelit.communicationplatform.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(SessionScope::class)
class NotificationTroubleshootCheckPermissionTest(
    private val permissionStateProvider: PermissionStateProvider,
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    private val permissionActions: PermissionActions,
    stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order: Int = 0

    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_check_permission_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_check_permission_description),
        hasQuickFix = true,
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )

    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            permissionStateProvider.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        delegate.done(result)
    }

    override suspend fun reset() = delegate.reset()

    override suspend fun quickFix(
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        // Do not bother about asking the permission inline, just lead the user to the settings
        permissionActions.openSettings(Manifest.permission.POST_NOTIFICATIONS)
    }
}
