/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.tasks

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.push.test.notifications.FakeNotificationCleaner
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkAllRoomsAsReadTest {
    @Test
    fun `invoke - delegates to client and clears all notifications`() = runTest {
        val markAllRoomsAsReadLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = markAllRoomsAsReadLambda,
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isSuccess).isTrue()
        markAllRoomsAsReadLambda.assertions().isCalledOnce()
        clearAllMessagesEventsLambda.assertions().isCalledOnce().with(value(A_SESSION_ID))
    }

    @Test
    fun `invoke - does not clear notifications when client fails`() = runTest {
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { }
        val markAllRoomsAsRead = DefaultMarkAllRoomsAsRead(
            client = FakeMatrixClient(
                markAllRoomsAsReadResult = { Result.failure(IllegalStateException("Failed")) },
            ),
            notificationCleaner = FakeNotificationCleaner(
                clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markAllRoomsAsRead()

        assertThat(result.isFailure).isTrue()
        clearAllMessagesEventsLambda.assertions().isNeverCalled()
    }
}
