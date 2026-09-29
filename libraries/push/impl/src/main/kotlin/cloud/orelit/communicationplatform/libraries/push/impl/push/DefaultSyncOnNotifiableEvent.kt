/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClientProvider
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.push.impl.db.PushRequest
import cloud.orelit.communicationplatform.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(AppScope::class)
class DefaultSyncOnNotifiableEvent(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
) : SyncOnNotifiableEvent {
    override suspend operator fun invoke(requests: List<PushRequest>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        try {
            val eventsBySession = requests.groupBy { it.sessionId }

            appForegroundStateService.updateIsSyncingNotificationEvent(true)
            Timber.d("Starting opportunistic room list sync | In foreground: ${appForegroundStateService.isInForeground.value}")

            for ((sessionId, events) in eventsBySession) {
                val client = matrixClientProvider.getOrRestore(SessionId(sessionId)).getOrNull() ?: continue
                val roomIds = events.map { RoomId(it.roomId) }.distinct()

                client.roomListService.subscribeToVisibleRooms(roomIds)

                if (!appForegroundStateService.isInForeground.value) {
                    // Give the sync some time to complete in background
                    delay(10.seconds)
                }
            }
        } finally {
            Timber.d("Finished opportunistic room list sync")
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }
}
