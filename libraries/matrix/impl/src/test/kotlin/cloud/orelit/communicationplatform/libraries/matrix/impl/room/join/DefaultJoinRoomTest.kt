/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.room.join

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toRoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.impl.analytics.toAnalyticsJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ALIAS
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SERVER_LIST
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultJoinRoomTest {
    @Test
    fun `when using roomId and there is no server names, the classic join room API is used`() = runTest {
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), emptyList(), aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isNeverCalled()
        joinRoomLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ID)
            )
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }

    @Test
    fun `when using roomId and server names are available, joinRoomByIdOrAlias API is used`() = runTest {
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ID.toRoomIdOrAlias(), A_SERVER_LIST, aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ID.toRoomIdOrAlias()),
                value(A_SERVER_LIST)
            )
        joinRoomLambda
            .assertions()
            .isNeverCalled()
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }

    @Test
    fun `when using roomAlias, joinRoomByIdOrAlias API is used`() = runTest {
        val roomInfo = aRoomInfo()
        val joinRoomLambda = lambdaRecorder { _: RoomId -> Result.success(roomInfo) }
        val joinRoomByIdOrAliasLambda = lambdaRecorder { _: RoomIdOrAlias, _: List<String> -> Result.success(roomInfo) }
        val roomResult = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val aTrigger = JoinedRoom.Trigger.MobilePermalink
        val client: MatrixClient = FakeMatrixClient().also {
            it.joinRoomLambda = joinRoomLambda
            it.joinRoomByIdOrAliasLambda = joinRoomByIdOrAliasLambda
            it.givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = roomResult
            )
        }
        val analyticsService = FakeAnalyticsService()
        val sut = DefaultJoinRoom(
            client = client,
            analyticsService = analyticsService,
        )
        sut.invoke(A_ROOM_ALIAS.toRoomIdOrAlias(), A_SERVER_LIST, aTrigger)
        joinRoomByIdOrAliasLambda
            .assertions()
            .isCalledOnce()
            .with(
                value(A_ROOM_ALIAS.toRoomIdOrAlias()),
                value(emptyList<String>())
            )
        joinRoomLambda
            .assertions()
            .isNeverCalled()
        assertThat(analyticsService.capturedEvents).containsExactly(
            roomResult.toAnalyticsJoinedRoom(aTrigger)
        )
    }
}
