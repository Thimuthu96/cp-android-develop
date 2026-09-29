/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.verifysession.impl.incoming

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.verifysession.api.IncomingVerificationEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultIncomingVerificationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultIncomingVerificationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            IncomingVerificationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { _, _ -> createPresenter() }
            )
        }
        val callback = object : IncomingVerificationEntryPoint.Callback {
            override fun onDone() = lambdaError()
        }
        val params = IncomingVerificationEntryPoint.Params(
            verificationRequest = anIncomingSessionVerificationRequest()
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(IncomingVerificationNode::class.java)
        assertThat(result.plugins).contains(params)
        assertThat(result.plugins).contains(callback)
    }
}
