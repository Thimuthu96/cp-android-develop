/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.impl.watchers

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.networkmonitor.api.NetworkMonitor
import cloud.orelit.communicationplatform.features.networkmonitor.api.NetworkStatus
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.core.coroutine.childScope
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomListService
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsRoomListStateWatcher
import cloud.orelit.communicationplatform.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

@ContributesBinding(SessionScope::class)
class DefaultAnalyticsRoomListStateWatcher(
    private val appForegroundStateService: AppForegroundStateService,
    private val networkMonitor: NetworkMonitor,
    private val roomListService: RoomListService,
    private val analyticsService: AnalyticsService,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    dispatchers: CoroutineDispatchers,
) : AnalyticsRoomListStateWatcher {
    private val coroutineScope: CoroutineScope = sessionCoroutineScope.childScope(dispatchers.computation, "AnalyticsRoomListStateWatcher")
    private val isStarted = AtomicBoolean(false)
    private val isNotInitialSync get() = roomListService.isInitialSyncDone

    override fun start() {
        if (isStarted.getAndSet(true)) {
            Timber.w("Can't start RoomListStateWatcher, it's already running.")
            return
        }

        val longRunningTransaction = AnalyticsLongRunningTransaction.CatchUp

        val hasNetworkConnectivityFlow = networkMonitor.connectivity
            .map { it == NetworkStatus.Connected }
            .distinctUntilChanged()

        combine(
            appForegroundStateService.isInForeground,
            hasNetworkConnectivityFlow,
        ) { isInForeground, hasNetworkConnectivity ->
            val canSync = isInForeground && hasNetworkConnectivity
            val isNotSyncing = roomListService.state.value != RoomListService.State.Running
                if (isNotInitialSync && canSync && isNotSyncing) {
                    Timber.d("Catch-up transaction: starting")
                    analyticsService.startLongRunningTransaction(longRunningTransaction)
                } else if (!isInForeground || !hasNetworkConnectivity) {
                    analyticsService.removeLongRunningTransaction(longRunningTransaction)?.let {
                        Timber.d("Catch-up transaction: stopping")
                    }
                }
            }
            .launchIn(coroutineScope)

        roomListService.state
            .onEach { state ->
                if (state == RoomListService.State.Running && isNotInitialSync) {
                    val transaction = analyticsService.removeLongRunningTransaction(longRunningTransaction)
                    if (transaction != null && !transaction.isFinished()) {
                        val duration = transaction.duration
                        if (duration > 3.minutes) {
                            Timber.d("Cancelling catch-up transaction, the elapsed time is too long ($duration), something probably went wrong while measuring")
                        } else {
                            Timber.d("Catch-up transaction finished in $duration")
                            transaction.finish()
                        }
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    override fun stop() {
        if (isStarted.getAndSet(false)) {
            coroutineScope.coroutineContext.cancelChildren()
        }
    }
}
