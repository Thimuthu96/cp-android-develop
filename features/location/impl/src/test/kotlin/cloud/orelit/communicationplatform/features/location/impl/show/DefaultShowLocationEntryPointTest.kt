/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.show

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.location.api.Location
import cloud.orelit.communicationplatform.features.location.api.ShowLocationEntryPoint
import cloud.orelit.communicationplatform.features.location.api.ShowLocationMode
import cloud.orelit.communicationplatform.features.location.impl.common.FakeUserLocationStateFactory
import cloud.orelit.communicationplatform.features.location.impl.common.actions.FakeLocationActions
import cloud.orelit.communicationplatform.features.location.impl.common.permissions.FakePermissionsPresenter
import cloud.orelit.communicationplatform.features.location.test.FakeActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.services.toolbox.test.strings.FakeStringProvider
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import org.junit.Rule
import org.junit.Test

class DefaultShowLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() {
        val entryPoint = DefaultShowLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            val joinedRoom = FakeJoinedRoom()
            ShowLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { mode ->
                    ShowLocationPresenter(
                        mode = mode,
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        dateFormatter = FakeDateFormatter(),
                        stringProvider = FakeStringProvider(),
                        joinedRoom = joinedRoom,
                        liveLocationShareManager = FakeActiveLiveLocationShareManager(),
                        userLocationStateFactory = FakeUserLocationStateFactory(),
                        customMapTilerConfigProvider = { Result.success(null) },
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val inputs = ShowLocationEntryPoint.Inputs(
            mode = ShowLocationMode.Static(
                location = Location(37.4219983, -122.084, 10f),
                senderName = "Alice",
                senderId = UserId("@alice:matrix.org"),
                senderAvatarUrl = null,
                timestamp = System.currentTimeMillis(),
                assetType = null,
            ),
        )
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            inputs = inputs,
        )
        assertThat(result).isInstanceOf(ShowLocationNode::class.java)
        assertThat(result.plugins).contains(inputs)
    }
}
