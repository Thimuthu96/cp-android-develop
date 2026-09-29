/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.tasks

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.invite.test.InMemorySeenInvitesStore
import cloud.orelit.communicationplatform.features.preferences.impl.DefaultCacheService
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.push.test.FakePushService
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemoryCacheStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aCacheData
import cloud.orelit.communicationplatform.services.appnavstate.impl.DefaultActiveRoomsHolder
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test

class DefaultClearCacheUseCaseTest : RobolectricTest() {
    @Test
    fun `execute clear cache should do all the expected tasks`() = runTest {
        val activeRoomsHolder = DefaultActiveRoomsHolder().apply { addRoom(FakeJoinedRoom()) }
        val clearCacheLambda = lambdaRecorder<Unit> { }
        val matrixClient = FakeMatrixClient(
            sessionId = A_SESSION_ID,
            clearCacheLambda = clearCacheLambda,
        )
        val defaultCacheService = DefaultCacheService()
        val setIgnoreRegistrationErrorLambda = lambdaRecorder<SessionId, Boolean, Unit> { _, _ -> }
        val resetBatteryOptimizationStateResult = lambdaRecorder<Unit> { }
        val pushService = FakePushService(
            setIgnoreRegistrationErrorLambda = setIgnoreRegistrationErrorLambda,
            resetBatteryOptimizationStateResult = resetBatteryOptimizationStateResult,
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID))
        assertThat(seenInvitesStore.seenRoomIds().first()).isNotEmpty()
        val cacheStore = InMemoryCacheStore(
            initialData = mapOf("key1" to aCacheData())
        )
        val sut = DefaultClearCacheUseCase(
            context = InstrumentationRegistry.getInstrumentation().context,
            matrixClient = matrixClient,
            coroutineDispatchers = testCoroutineDispatchers(),
            defaultCacheService = defaultCacheService,
            okHttpClient = { OkHttpClient.Builder().build() },
            pushService = pushService,
            seenInvitesStore = seenInvitesStore,
            activeRoomsHolder = activeRoomsHolder,
            cacheStore = cacheStore,
        )
        defaultCacheService.clearedCacheEventFlow.test {
            sut.invoke()
            assertThat(cacheStore.dataMap).isEmpty()
            clearCacheLambda.assertions().isCalledOnce()
            setIgnoreRegistrationErrorLambda.assertions().isCalledOnce()
                .with(value(matrixClient.sessionId), value(false))
            resetBatteryOptimizationStateResult.assertions().isCalledOnce()
            assertThat(awaitItem()).isEqualTo(matrixClient.sessionId)
            assertThat(seenInvitesStore.seenRoomIds().first()).isEmpty()
            assertThat(activeRoomsHolder.getActiveRoom(A_SESSION_ID)).isNull()
        }
    }
}
