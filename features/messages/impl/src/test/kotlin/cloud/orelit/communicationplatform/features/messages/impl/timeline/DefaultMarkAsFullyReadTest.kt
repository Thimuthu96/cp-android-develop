/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultMarkAsFullyReadTest {
    @Test
    fun `When marking as read fails, no exception is thrown`() = runTest {
        val markAsFullyRead = DefaultMarkAsFullyRead(
            matrixClient = FakeMatrixClient(
                markRoomAsFullyReadResult = { _, _ -> Result.failure(IllegalStateException("Room not found")) },
            ).apply {
                givenGetRoomResult(A_ROOM_ID, null)
            },
            coroutineDispatchers = testCoroutineDispatchers(),
        )
        assertThat(markAsFullyRead.invoke(A_ROOM_ID, AN_EVENT_ID).isFailure).isTrue()
        runCurrent()
    }

    @Test
    fun `When marking as read is successful, the expected method is invoked`() = runTest {
        val markAsFullyReadResult = lambdaRecorder<RoomId, EventId, Result<Unit>> { _, _ -> Result.success(Unit) }
        val markAsFullyRead = DefaultMarkAsFullyRead(
            matrixClient = FakeMatrixClient(
                markRoomAsFullyReadResult = markAsFullyReadResult,
            ),
            coroutineDispatchers = testCoroutineDispatchers(),
        )
        assertThat(markAsFullyRead.invoke(A_ROOM_ID, AN_EVENT_ID).isSuccess).isTrue()
        runCurrent()
        markAsFullyReadResult.assertions().isCalledOnce().with(value(A_ROOM_ID), value(AN_EVENT_ID))
    }
}
