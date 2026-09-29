/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.call.test.FakeElementCallEntryPoint
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEntryPoint
import cloud.orelit.communicationplatform.features.verifysession.test.FakeOutgoingVerificationEntryPoint
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeMediaViewerEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultUserProfileEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultUserProfileEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            UserProfileFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                sessionId = A_SESSION_ID,
                elementCallEntryPoint = FakeElementCallEntryPoint(),
                mediaViewerEntryPoint = FakeMediaViewerEntryPoint(),
                outgoingVerificationEntryPoint = FakeOutgoingVerificationEntryPoint(),
            )
        }
        val callback = object : UserProfileEntryPoint.Callback {
            override fun navigateToRoom(roomId: RoomId) {
                lambdaError()
            }
        }
        val params = UserProfileEntryPoint.Params(
            userId = A_USER_ID,
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(UserProfileFlowNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}
