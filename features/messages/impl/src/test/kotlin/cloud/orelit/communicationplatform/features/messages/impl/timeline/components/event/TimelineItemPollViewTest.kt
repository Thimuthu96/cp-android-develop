/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.event

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineEvent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.pressTag
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class TimelineItemPollViewTest : RobolectricTest() {
    @Test
    fun `answering a poll with first answer should emit a PollAnswerSelected event`() {
        testAnswer(answerIndex = 0)
    }

    @Test
    fun `answering a poll with second answer should emit a PollAnswerSelected event`() {
        testAnswer(answerIndex = 1)
    }

    private fun testAnswer(answerIndex: Int) = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<TimelineEvent.TimelineItemPollEvent>()
        val content = aTimelineItemPollContent()
        setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        val answer = content.answerItems[answerIndex].answer
        onNode(
            matcher = hasText(answer.text),
            useUnmergedTree = true,
        ).performClick()
        eventsRecorder.assertSingle(TimelineEvent.SelectPollAnswer(content.eventId!!, answer.id))
    }

    @Test
    fun `editing a poll should emit a PollEditClicked event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent.TimelineItemPollEvent>()
        val content = aTimelineItemPollContent(
            isMine = true,
            isEditable = true,
        )
        setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        clickOn(CommonStrings.action_edit_poll)
        eventsRecorder.assertSingle(TimelineEvent.EditPoll(content.eventId!!))
    }

    @Test
    fun `closing a poll should emit a PollEndClicked event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<TimelineEvent.TimelineItemPollEvent>()
        val content = aTimelineItemPollContent(
            isMine = true,
        )
        setContent {
            TimelineItemPollView(
                content = content,
                eventSink = eventsRecorder
            )
        }
        clickOn(CommonStrings.action_end_poll)
        // A confirmation dialog should be shown
        eventsRecorder.assertEmpty()
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(TimelineEvent.EndPoll(content.eventId!!))
    }
}
