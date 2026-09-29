/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.share

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bumble.appyx.core.modality.BuildContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.location.impl.common.FakeUserLocationStateFactory
import cloud.orelit.communicationplatform.features.location.impl.common.actions.FakeLocationActions
import cloud.orelit.communicationplatform.features.location.impl.common.permissions.FakePermissionsPresenter
import cloud.orelit.communicationplatform.features.location.impl.live.LiveLocationStore
import cloud.orelit.communicationplatform.features.location.test.FakeActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.features.messages.test.FakeMessageComposerContext
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDurationFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.preferences.test.FakePreferenceDataStoreFactory
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultShareLocationEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultShareLocationEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            val room = FakeJoinedRoom()
            ShareLocationNode(
                buildContext = buildContext,
                plugins = plugins,
                presenterFactory = { timelineMode: Timeline.Mode ->
                    ShareLocationPresenter(
                        permissionsPresenterFactory = { FakePermissionsPresenter() },
                        room = room,
                        timelineMode = timelineMode,
                        analyticsService = FakeAnalyticsService(),
                        messageComposerContext = FakeMessageComposerContext(),
                        locationActions = FakeLocationActions(),
                        buildMeta = aBuildMeta(),
                        client = FakeMatrixClient(),
                        durationFormatter = FakeDurationFormatter(),
                        liveLocationShareManager = FakeActiveLiveLocationShareManager(),
                        liveLocationStore = LiveLocationStore(
                            preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
                            sessionId = room.sessionId,
                        ),
                        userLocationStateFactory = FakeUserLocationStateFactory(),
                        customMapTilerConfigProvider = { Result.success(null) },
                    )
                },
                analyticsService = FakeAnalyticsService(),
            )
        }
        val timelineMode = Timeline.Mode.Live
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            timelineMode = timelineMode,
        )
        assertThat(result).isInstanceOf(ShareLocationNode::class.java)
        assertThat(result.plugins).contains(ShareLocationNode.Inputs(timelineMode))
    }
}
