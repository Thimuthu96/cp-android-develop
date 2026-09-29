/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.NotificationTroubleShootEntryPoint
import cloud.orelit.communicationplatform.services.analytics.test.FakeScreenTracker
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultNotificationTroubleShootEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultNotificationTroubleShootEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            TroubleshootNotificationsNode(
                buildContext = buildContext,
                plugins = plugins,
                factory = { createTroubleshootNotificationsPresenter() },
                screenTracker = FakeScreenTracker(),
            )
        }
        val callback = object : NotificationTroubleShootEntryPoint.Callback {
            override fun onDone() = lambdaError()
            override fun navigateToBlockedUsers() = lambdaError()
        }
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            callback = callback,
        )
        assertThat(result).isInstanceOf(TroubleshootNotificationsNode::class.java)
        assertThat(result.plugins).contains(callback)
    }
}
