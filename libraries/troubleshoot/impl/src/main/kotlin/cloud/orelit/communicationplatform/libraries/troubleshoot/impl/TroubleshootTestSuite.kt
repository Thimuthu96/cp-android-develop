/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl

import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.NotificationTroubleshoot
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.push.api.GetCurrentPushProvider
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.TestFilterData
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Inject
class TroubleshootTestSuite(
    private val sessionId: SessionId,
    private val notificationTroubleshootTests: Set<@JvmSuppressWildcards NotificationTroubleshootTest>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
    private val analyticsService: AnalyticsService,
) {
    lateinit var tests: List<NotificationTroubleshootTest>

    private val _state: MutableStateFlow<TroubleshootTestSuiteState> = MutableStateFlow(
        TroubleshootTestSuiteState(
            mainState = AsyncAction.Uninitialized,
            tests = emptyList<NotificationTroubleshootTestState>().toImmutableList()
        )
    )
    val state: StateFlow<TroubleshootTestSuiteState> = _state

    suspend fun start(coroutineScope: CoroutineScope) {
        val testFilterData = TestFilterData(
            currentPushProviderName = getCurrentPushProvider.getCurrentPushProvider(sessionId)
        )
        tests = notificationTroubleshootTests
            .filter { it.isRelevant(testFilterData) }
            .sortedBy { it.order }
        tests.forEach {
            // Observe the state of the tests
            it.state.onEach {
                emitState()
            }.launchIn(coroutineScope)
        }
    }

    suspend fun runTestSuite(coroutineScope: CoroutineScope) {
        tests.forEach {
            it.reset()
        }
        tests.forEach {
            it.run(coroutineScope)
        }
    }

    suspend fun retryFailedTest(coroutineScope: CoroutineScope) {
        tests
            .filter { it.state.value.status is NotificationTroubleshootTestState.Status.Failure }
            .forEach {
                it.run(coroutineScope)
            }
    }

    private suspend fun emitState() {
        val states = tests.map { it.state.value }
        val mainState = states.computeMainState()
        when (mainState) {
            is AsyncAction.Success -> {
                analyticsService.capture(NotificationTroubleshoot(hasError = false))
            }
            is AsyncAction.Failure -> {
                analyticsService.capture(NotificationTroubleshoot(hasError = true))
            }
            else -> Unit
        }
        _state.emit(
            TroubleshootTestSuiteState(
                mainState = states.computeMainState(),
                tests = states.toImmutableList()
            )
        )
    }

    suspend fun quickFix(
        testIndex: Int,
        coroutineScope: CoroutineScope,
        navigator: NotificationTroubleshootNavigator,
    ) {
        tests[testIndex].quickFix(coroutineScope, navigator)
    }
}

fun List<NotificationTroubleshootTestState>.computeMainState(): AsyncAction<Unit> {
    val isIdle = all { it.status is NotificationTroubleshootTestState.Status.Idle }
    val isRunning = any { it.status is NotificationTroubleshootTestState.Status.InProgress }
    return when {
        isIdle -> AsyncAction.Uninitialized
        isRunning -> AsyncAction.Loading
        else -> {
            if (any { it.status is NotificationTroubleshootTestState.Status.WaitingForUser }) {
                AsyncAction.ConfirmingNoParams
            } else if (any { it.status.let { status -> status is NotificationTroubleshootTestState.Status.Failure && status.isCritical } }) {
                AsyncAction.Failure(Exception("Some tests failed"))
            } else {
                AsyncAction.Success(Unit)
            }
        }
    }
}
