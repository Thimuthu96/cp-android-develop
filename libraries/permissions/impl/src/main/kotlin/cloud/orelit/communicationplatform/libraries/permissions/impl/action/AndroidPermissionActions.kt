/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.permissions.impl.action

import android.Manifest
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.androidutils.system.openAppSettingsPage
import cloud.orelit.communicationplatform.libraries.androidutils.system.startNotificationSettingsIntent
import cloud.orelit.communicationplatform.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
class AndroidPermissionActions(
    @ApplicationContext private val context: Context
) : PermissionActions {
    override fun openSettings(permission: String) {
        when (permission) {
            Manifest.permission.POST_NOTIFICATIONS -> context.startNotificationSettingsIntent()
            else -> context.openAppSettingsPage()
        }
    }
}
