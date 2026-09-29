/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.runtime.Composable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.testing.junit4.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.call.test.FakeElementCallEntryPoint
import cloud.orelit.communicationplatform.features.forward.test.FakeForwardEntryPoint
import cloud.orelit.communicationplatform.features.knockrequests.test.FakeKnockRequestsListEntryPoint
import cloud.orelit.communicationplatform.features.location.test.FakeLocationService
import cloud.orelit.communicationplatform.features.location.test.FakeShareLocationEntryPoint
import cloud.orelit.communicationplatform.features.location.test.FakeShowLocationEntryPoint
import cloud.orelit.communicationplatform.features.messages.api.MessagesEntryPoint
import cloud.orelit.communicationplatform.features.messages.impl.pinned.banner.createPinnedEventsTimelineProvider
import cloud.orelit.communicationplatform.features.messages.impl.timeline.createTimelineController
import cloud.orelit.communicationplatform.features.poll.test.create.FakeCreatePollEntryPoint
import cloud.orelit.communicationplatform.libraries.androidutils.system.DeviceHasVulkanSupport
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.roomlist.FakeRoomListService
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.RoomMemberProfilesCache
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.RoomNamesCache
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeMediaViewerEntryPoint
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanTheme
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanUpdater
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.node.TestParentNode
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultMessagesEntryPointTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `test node builder`() = runTest {
        val entryPoint = DefaultMessagesEntryPoint()
        val parentNode = TestParentNode.create { buildContext, plugins ->
            MessagesFlowNode(
                buildContext = buildContext,
                plugins = plugins,
                roomListService = FakeRoomListService(),
                sessionId = A_SESSION_ID,
                shareLocationEntryPoint = FakeShareLocationEntryPoint(),
                showLocationEntryPoint = FakeShowLocationEntryPoint(),
                createPollEntryPoint = FakeCreatePollEntryPoint(),
                elementCallEntryPoint = FakeElementCallEntryPoint(),
                mediaViewerEntryPoint = FakeMediaViewerEntryPoint(),
                forwardEntryPoint = FakeForwardEntryPoint(),
                analyticsService = FakeAnalyticsService(),
                locationService = FakeLocationService(),
                room = FakeBaseRoom(),
                roomMemberProfilesCache = RoomMemberProfilesCache(),
                roomNamesCache = RoomNamesCache(),
                mentionSpanUpdater = object : MentionSpanUpdater {
                    override fun updateMentionSpans(text: CharSequence) = text

                    @Composable
                    override fun rememberMentionSpans(text: CharSequence) = text
                },
                mentionSpanTheme = MentionSpanTheme(A_USER_ID),
                pinnedEventsTimelineProvider = createPinnedEventsTimelineProvider(),
                timelineController = createTimelineController(),
                knockRequestsListEntryPoint = FakeKnockRequestsListEntryPoint(),
                dateFormatter = FakeDateFormatter(),
                coroutineDispatchers = testCoroutineDispatchers(),
                hasVulkanSupport = DeviceHasVulkanSupport(mockk(relaxed = true))
            )
        }
        val callback = object : MessagesEntryPoint.Callback {
            override fun navigateToRoomDetails() = lambdaError()
            override fun navigateToRoomMemberDetails(userId: UserId) = lambdaError()
            override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) = lambdaError()
            override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) = lambdaError()
            override fun navigateToRoom(roomId: RoomId) = lambdaError()
            override fun navigateToDeveloperSettings() = lambdaError()
        }
        val initialTarget = MessagesEntryPoint.InitialTarget.Messages(focusedEventId = AN_EVENT_ID)
        val params = MessagesEntryPoint.Params(initialTarget)
        val result = entryPoint.createNode(
            parentNode = parentNode,
            buildContext = BuildContext.root(null),
            params = params,
            callback = callback,
        )
        assertThat(result).isInstanceOf(MessagesFlowNode::class.java)
        assertThat(result.plugins).contains(MessagesEntryPoint.Params(initialTarget))
        assertThat(result.plugins).contains(callback)
    }

    @Test
    fun `test initial target to nav target mapping`() {
        assertThat(MessagesEntryPoint.InitialTarget.Messages(focusedEventId = AN_EVENT_ID).toNavTarget())
            .isEqualTo(MessagesFlowNode.NavTarget.Messages(focusedEventId = AN_EVENT_ID))
        assertThat(MessagesEntryPoint.InitialTarget.PinnedMessages.toNavTarget())
            .isEqualTo(MessagesFlowNode.NavTarget.PinnedMessagesList)
    }
}
