/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.unregistration

import androidx.compose.ui.graphics.toArgb
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.appconfig.NotificationConfig
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.NotificationDisplayer
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories.NotificationAccountParams
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories.NotificationCreator
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import kotlinx.coroutines.flow.first

interface ServiceUnregisteredHandler {
    suspend fun handle(userId: UserId)
}

@ContributesBinding(AppScope::class)
class DefaultServiceUnregisteredHandler(
    private val enterpriseService: EnterpriseService,
    private val notificationCreator: NotificationCreator,
    private val notificationDisplayer: NotificationDisplayer,
    private val sessionStore: SessionStore,
) : ServiceUnregisteredHandler {
    override suspend fun handle(userId: UserId) {
        val color = enterpriseService.brandColorsFlow(userId).first()?.toArgb()
            ?: NotificationConfig.NOTIFICATION_ACCENT_COLOR
        val hasMultipleAccounts = sessionStore.numberOfSessions() > 1
        val notification = notificationCreator.createUnregistrationNotification(
            NotificationAccountParams(
                user = MatrixUser(userId),
                color = color,
                showSessionId = hasMultipleAccounts,
            )
        )
        notificationDisplayer.displayUnregistrationNotification(notification)
    }
}
