/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.loggedin

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SyncState
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.sync.FakeSyncService
import cloud.orelit.communicationplatform.tests.testutils.lambda.assert
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendQueuesTest {
    private val matrixClient = FakeMatrixClient()
    private val syncService = FakeSyncService(initialSyncState = SyncState.Running)
    private val sut = SendQueues(matrixClient, syncService)

    @Test
    fun `test network status online and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)
        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeJoinedRoom(
            setSendQueueEnabledResult = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)
        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda)
            .isCalledOnce()
            .with(value(true))

        assert(setRoomSendQueueEnabledLambda).isNeverCalled()
    }

    @Test
    fun `test sync state offline and sending queue failed`() = runTest {
        val sendQueueDisabledFlow = MutableSharedFlow<RoomId>(replay = 1)

        val setAllSendQueuesEnabledLambda = lambdaRecorder { _: Boolean -> }
        matrixClient.sendQueueDisabledFlow = sendQueueDisabledFlow
        matrixClient.setAllSendQueuesEnabledLambda = setAllSendQueuesEnabledLambda
        syncService.emitSyncState(SyncState.Offline)
        val setRoomSendQueueEnabledLambda = lambdaRecorder { _: Boolean -> }
        val room = FakeJoinedRoom(
            setSendQueueEnabledResult = setRoomSendQueueEnabledLambda
        )
        matrixClient.givenGetRoomResult(room.roomId, room)

        sut.launchIn(backgroundScope)

        sendQueueDisabledFlow.emit(room.roomId)
        advanceTimeBy(SEND_QUEUES_RETRY_DELAY_MILLIS)
        runCurrent()

        assert(setAllSendQueuesEnabledLambda).isNeverCalled()
        assert(setRoomSendQueueEnabledLambda).isNeverCalled()
    }
}
