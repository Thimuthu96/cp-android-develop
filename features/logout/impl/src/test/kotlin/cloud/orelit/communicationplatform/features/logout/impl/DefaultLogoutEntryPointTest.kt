/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.logout.api.LogoutEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultLogoutEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultLogoutEntryPoint()

        val parentNode = TestParentNode.create { buildContext, plugins ->
            LogoutNode(
                buildContext = buildContext,
                plugins = plugins,
                presenter = createLogoutPresenter(),
            )
        }
        val callback = object : LogoutEntryPoint.Callback {
            override fun navigateToSecureBackup() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(LogoutNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
