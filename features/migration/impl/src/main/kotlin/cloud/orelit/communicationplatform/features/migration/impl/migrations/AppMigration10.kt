/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.migration.impl.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestType
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerScheduler

/**
 * Remove existing fetch notifications work manager requests since their format has changed.
 */
@ContributesIntoSet(AppScope::class)
class AppMigration10(
    private val sessionStore: SessionStore,
    private val workManagerScheduler: WorkManagerScheduler,
) : AppMigration {
    override val order: Int = 10

    override suspend fun migrate(isFreshInstall: Boolean) {
        if (isFreshInstall) return

        val sessions = sessionStore.getAllSessions()

        for (session in sessions) {
            workManagerScheduler.cancel(
                sessionId = SessionId(session.userId),
                requestType = WorkManagerRequestType.NOTIFICATION_SYNC
            )
        }
    }
}
