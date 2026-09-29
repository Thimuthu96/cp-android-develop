/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import cloud.orelit.communicationplatform.features.home.api.HomeEntryPoint
import cloud.orelit.communicationplatform.features.invite.test.declineandblock.FakeDeclineInviteAndBlockEntryPoint
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultHomeEntryPointTest : RobolectricTest() {
    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultHomeEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            HomeFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                matrixClient = FakeMatrixClient(),
                presenter = createHomePresenter(),
                inviteFriendsUseCase = { lambdaError() },
                analyticsService = FakeAnalyticsService(),
                acceptDeclineInviteView = { _, _, _, _ -> lambdaError() },
                directLogoutView = { _ -> lambdaError() },
                reportRoomEntryPoint = { _, _, _ -> lambdaError() },
                declineInviteAndBlockUserEntryPoint = FakeDeclineInviteAndBlockEntryPoint(),
                changeRoomMemberRolesEntryPoint = { _, _, _, _ -> lambdaError() },
                leaveRoomRenderer = { _, _, _ -> lambdaError() },
                sessionCoroutineScope = backgroundScope,
            )
        }
        val callback = object : HomeEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId, eventId: EventId?, joinedRoom: JoinedRoom?) = lambdaError()
            override fun navigateToCreateRoom() = lambdaError()
            override fun navigateToCreateSpace() = lambdaError()
            override fun navigateToSettings() = lambdaError()
            override fun navigateToSetUpRecovery() = lambdaError()
            override fun navigateToEnterRecoveryKey() = lambdaError()
            override fun navigateToRoomSettings(roomId: RoomId) = lambdaError()
            override fun navigateToBugReport() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(HomeFlowNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
