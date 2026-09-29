/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.features.preferences.impl.tasks

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.ReceiptType
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.preferences.test.InMemorySessionPreferencesStore
import cloud.orelit.communicationplatform.libraries.push.test.notifications.FakeNotificationCleaner
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MarkRoomAsReadTest {
    @Test
    fun `invoke - clears notifications, clears unread flag and sends public read receipt`() = runTest {
        val markAsReadResult = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val room = FakeBaseRoom(markAsReadResult = markAsReadResult)
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val clearMessagesForRoomLambda = lambdaRecorder { _: SessionId, _: RoomId -> }
        val notificationCleaner = FakeNotificationCleaner(clearMessagesForRoomLambda = clearMessagesForRoomLambda)
        val sessionPreferencesStore = InMemorySessionPreferencesStore()
        val markRoomAsRead = DefaultMarkRoomAsRead(
            client = matrixClient,
            notificationCleaner = notificationCleaner,
            sessionPreferencesStore = sessionPreferencesStore,
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markRoomAsRead(A_ROOM_ID)
        runCurrent()

        assertThat(result.isSuccess).isTrue()
        clearMessagesForRoomLambda.assertions().isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        assertThat(room.setUnreadFlagCalls).isEqualTo(listOf(false))
        markAsReadResult.assertions().isCalledOnce().with(value(ReceiptType.READ))
    }

    @Test
    fun `invoke - sends private read receipt when public receipts are disabled`() = runTest {
        val markAsReadResult = lambdaRecorder<ReceiptType, Result<Unit>> { Result.success(Unit) }
        val room = FakeBaseRoom(markAsReadResult = markAsReadResult)
        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSendPublicReadReceiptsEnabled = false)
        val markRoomAsRead = DefaultMarkRoomAsRead(
            client = matrixClient,
            notificationCleaner = FakeNotificationCleaner(clearMessagesForRoomLambda = { _, _ -> }),
            sessionPreferencesStore = sessionPreferencesStore,
            coroutineDispatchers = testCoroutineDispatchers(),
        )

        val result = markRoomAsRead(A_ROOM_ID)
        runCurrent()

        assertThat(result.isSuccess).isTrue()
        markAsReadResult.assertions().isCalledOnce().with(value(ReceiptType.READ_PRIVATE))
    }
}
