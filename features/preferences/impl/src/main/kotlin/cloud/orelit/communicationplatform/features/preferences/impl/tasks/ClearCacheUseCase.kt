/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.tasks

import android.content.Context
import coil3.SingletonImageLoader
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.invite.api.SeenInvitesStore
import cloud.orelit.communicationplatform.features.preferences.impl.DefaultCacheService
import cloud.orelit.communicationplatform.libraries.cachestore.api.CacheStore
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.di.annotations.ApplicationContext
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.push.api.PushService
import cloud.orelit.communicationplatform.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

interface ClearCacheUseCase {
    suspend operator fun invoke()
}

@ContributesBinding(SessionScope::class)
class DefaultClearCacheUseCase(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val defaultCacheService: DefaultCacheService,
    private val okHttpClient: () -> OkHttpClient,
    private val pushService: PushService,
    private val seenInvitesStore: SeenInvitesStore,
    private val activeRoomsHolder: ActiveRoomsHolder,
    private val cacheStore: CacheStore,
) : ClearCacheUseCase {
    override suspend fun invoke() = withContext(coroutineDispatchers.io) {
        // Clear cache store
        cacheStore.deleteAll()
        // Active rooms should be disposed of before clearing the cache
        activeRoomsHolder.clear(matrixClient.sessionId)
        // Clear Matrix cache
        matrixClient.clearCache()
        // Clear Coil cache
        SingletonImageLoader.get(context).let {
            it.diskCache?.clear()
            it.memoryCache?.clear()
        }
        // Clear OkHttp cache
        okHttpClient().cache?.delete()
        // Clear app cache
        context.cacheDir?.listFiles {
            // But keep the logs
            it.name != "logs"
        }?.onEach {
            it.deleteRecursively()
        }
        // Clear some settings
        seenInvitesStore.clear()
        // Ensure any error will be displayed again
        pushService.setIgnoreRegistrationError(matrixClient.sessionId, false)
        pushService.resetBatteryOptimizationState()
        // Ensure the app is restarted
        defaultCacheService.onClearedCache(matrixClient.sessionId)
    }
}
