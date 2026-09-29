/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.featureflag.test.FakeFeatureFlagService
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.impl.auth.FakeProxyProvider
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.FakeTimelineEventFilterFactory
import cloud.orelit.communicationplatform.libraries.matrix.impl.storage.FakeSqliteStoreBuilderProvider
import cloud.orelit.communicationplatform.libraries.network.useragent.SimpleUserAgentProvider
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aSessionData
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestBuilder
import cloud.orelit.communicationplatform.libraries.workmanager.test.FakeWorkManagerScheduler
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustMatrixClientFactoryTest {
    @Test
    fun test() = runTest {
        val scheduleVacuumLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = scheduleVacuumLambda)
        val sut = createRustMatrixClientFactory(workManagerScheduler = workManagerScheduler)

        val result = sut.create(aSessionData())

        assertThat(result.sessionId).isEqualTo(SessionId("@alice:server.org"))
        scheduleVacuumLambda.assertions().isCalledOnce()
        result.destroy()
    }
}

fun TestScope.createRustMatrixClientFactory(
    cacheDirectory: File = File("/cache"),
    sessionStore: SessionStore = InMemorySessionStore(
        updateUserProfileResult = { _, _, _ -> },
    ),
    clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
    workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
) = RustMatrixClientFactory(
    cacheDirectory = cacheDirectory,
    appCoroutineScope = backgroundScope,
    coroutineDispatchers = testCoroutineDispatchers(),
    sessionStore = sessionStore,
    userAgentProvider = SimpleUserAgentProvider(),
    proxyProvider = FakeProxyProvider(),
    clock = FakeSystemClock(),
    analyticsService = FakeAnalyticsService(),
    featureFlagService = FakeFeatureFlagService(),
    timelineEventFilterFactory = FakeTimelineEventFilterFactory(),
    clientBuilderProvider = clientBuilderProvider,
    sqliteStoreBuilderProvider = FakeSqliteStoreBuilderProvider(),
    workManagerScheduler = workManagerScheduler,
    clientBuilderEnterpriseHook = { builder, _ -> builder },
)
