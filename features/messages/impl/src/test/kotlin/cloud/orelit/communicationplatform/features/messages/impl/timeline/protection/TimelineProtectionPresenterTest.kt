/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.protection

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.contentscanner.api.ContentScannerService
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaPreviewConfig
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaPreviewService
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaPreviewValue
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.room.BaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.media.FakeMediaPreviewService
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TimelineProtectionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderAll)
        }
    }

    @Test
    fun `present - media preview value off`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Off, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf()))
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
    }

    @Test
    fun `present - media preview value private in public room`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Public))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService, room = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf()))
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.protectionState).isEqualTo(ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID)))
        }
    }

    @Test
    fun `present - media preview value private in non public room`() = runTest {
        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite))
        val presenter = createPresenter(mediaPreviewService = mediaPreviewService, room = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.protectionState).isEqualTo(ProtectionState.RenderAll)
            // ShowContent with null should have no effect.
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = null))
            initialState.eventSink(TimelineProtectionEvent.ShowContent(eventId = AN_EVENT_ID))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - validate media scans the media source`() = runTest {
        val url = "https://example.com/media"

        val mediaPreviewConfig = MediaPreviewConfig(mediaPreviewValue = MediaPreviewValue.Private, hideInviteAvatar = false)
        val mediaPreviewService = FakeMediaPreviewService(mediaPreviewConfigFlow = MutableStateFlow(mediaPreviewConfig))
        val room = FakeBaseRoom(initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite), roomCoroutineScope = backgroundScope)
        val contentScannerService = lambdaRecorder { _: List<MediaSource>, state: ContentValidationState ->
            state.update(url, ContentValidationValue.Valid)
        }
        val presenter = createPresenter(
            mediaPreviewService = mediaPreviewService,
            room = room,
            contentScannerService = contentScannerService,
        )
        presenter.test {
            val validationState = DefaultContentValidationState(initial = mapOf(url to ContentValidationValue.Unknown))
            assertThat(validationState.getCurrentOverallState().isValid()).isFalse()

            val initialState = awaitItem()
            initialState.eventSink(
                TimelineProtectionEvent.ValidateContent(
                    mediaSources = listOf(MediaSource(url)),
                    validationState = validationState
                )
            )

            runCurrent()

            contentScannerService.assertions().isCalledOnce()
            assertThat(validationState.getCurrentOverallState().isValid()).isTrue()
        }
    }

    private fun createPresenter(
        room: BaseRoom = FakeBaseRoom(),
        mediaPreviewService: MediaPreviewService = FakeMediaPreviewService(),
        contentScannerService: ContentScannerService = ContentScannerService { _: List<MediaSource>, _: ContentValidationState -> },
    ) = TimelineProtectionPresenter(
        mediaPreviewService = mediaPreviewService,
        room = room,
        contentScannerService = contentScannerService,
    )
}
