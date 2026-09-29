/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.login.api.LoginEntryPoint
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.anAccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.classic.FakeElementClassicConnection
import cloud.orelit.communicationplatform.features.preferences.test.FakePreferencesEntryPoint
import cloud.orelit.communicationplatform.libraries.oauth.test.FakeOAuthActionFlow
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultLoginEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultLoginEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            LoginFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                accountProviderDataSource = anAccountProviderDataSource(),
                oAuthActionFlow = FakeOAuthActionFlow(),
                appCoroutineScope = backgroundScope,
                elementClassicConnection = FakeElementClassicConnection(),
                preferencesEntryPoint = FakePreferencesEntryPoint(),
            )
        }
        val callback = object : LoginEntryPoint.Callback {
            override fun navigateToBugReport() = lambdaError()
            override fun onDone() = lambdaError()
        }
        val params = LoginEntryPoint.Params(
            accountProvider = "ac",
            loginHint = "lh",
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(LoginFlowNode::class.java)
        assertThat(result.plugins).contains(LoginFlowNode.Params(params.accountProvider, params.loginHint))
        assertThat(result.plugins).contains(callback)
    }
}
