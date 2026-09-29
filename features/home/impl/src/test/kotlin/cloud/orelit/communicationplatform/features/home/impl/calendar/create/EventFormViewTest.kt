/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.assertNodeWithTextIsDisplayed
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import java.time.LocalDateTime

class EventFormViewTest : RobolectricTest() {
    @Test
    fun `typing the title generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        // The title is the first field, the description the second.
        onAllNodes(hasSetTextAction())[0].performTextInput("Sprint")
        waitForIdle()
        eventsRecorder.assertSingle(EventFormEvent.SetTitle("Sprint"))
    }

    @Test
    fun `typing the title one character at a time keeps them in order`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        // The state never changes, standing in for a presenter whose value always lags a frame behind.
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        val title = onAllNodes(hasSetTextAction())[0]

        "Abcd".forEach { character -> title.performTextInput(character.toString()) }
        waitForIdle()

        // The field owns its text and caret, so each keystroke lands after the previous one.
        title.assertTextContains("Abcd")
        eventsRecorder.assertList(
            listOf(
                EventFormEvent.SetTitle("A"),
                EventFormEvent.SetTitle("Ab"),
                EventFormEvent.SetTitle("Abc"),
                EventFormEvent.SetTitle("Abcd"),
            )
        )
    }

    @Test
    fun `typing the description one character at a time keeps them in order`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        val description = onAllNodes(hasSetTextAction())[1]

        "Abcd".forEach { character -> description.performTextInput(character.toString()) }
        waitForIdle()

        description.assertTextContains("Abcd")
        eventsRecorder.assertList(
            listOf(
                EventFormEvent.SetDescription("A"),
                EventFormEvent.SetDescription("Ab"),
                EventFormEvent.SetDescription("Abc"),
                EventFormEvent.SetDescription("Abcd"),
            )
        )
    }

    @Test
    fun `saving is not possible without a title`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent { EventFormView(state = anEventFormState(), modifier = Modifier) }
        onNode(hasText(activity!!.getString(CommonStrings.action_save)) and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun `saving generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent {
            EventFormView(
                state = anEventFormState(form = aNewEventForm(title = "Sprint"), eventSink = eventsRecorder),
                modifier = Modifier,
            )
        }
        onNode(hasText(activity!!.getString(CommonStrings.action_save)) and hasClickAction()).assertIsEnabled()
        clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(EventFormEvent.Save)
    }

    @Test
    fun `saving is not possible when the event ends before it starts`() = runAndroidComposeUiTest<ComponentActivity> {
        val form = aNewEventForm(title = "Sprint", end = aNewEventForm().start.minusHours(1))
        setContent { EventFormView(state = anEventFormState(form = form), modifier = Modifier) }
        onNode(hasText(activity!!.getString(CommonStrings.action_save)) and hasClickAction()).assertIsNotEnabled()
        assertNodeWithTextIsDisplayed(R.string.screen_calendar_event_end_before_start)
    }

    @Test
    fun `saving is not possible while the event is being sent`() = runAndroidComposeUiTest<ComponentActivity> {
        val state = anEventFormState(form = aNewEventForm(title = "Sprint"), saveAction = AsyncAction.Loading)
        setContent { EventFormView(state = state, modifier = Modifier) }
        // The button is replaced by a progress indicator.
        onNode(hasText(activity!!.getString(CommonStrings.action_save))).assertDoesNotExist()
    }

    @Test
    fun `closing an untouched form dismisses it`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        onNode(hasContentDescription(activity!!.getString(CommonStrings.action_close))).performClick()
        eventsRecorder.assertSingle(EventFormEvent.Dismiss)
    }

    @Test
    fun `closing a form with content asks for confirmation`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent {
            EventFormView(
                state = anEventFormState(form = aNewEventForm(title = "Sprint"), eventSink = eventsRecorder),
                modifier = Modifier,
            )
        }
        onNode(hasContentDescription(activity!!.getString(CommonStrings.action_close))).performClick()
        eventsRecorder.assertEmpty()
        assertNodeWithTextIsDisplayed(R.string.screen_calendar_event_discard_title)
        clickOn(CommonStrings.action_discard, inDialog = true)
        eventsRecorder.assertSingle(EventFormEvent.Dismiss)
    }

    @Test
    fun `cancelling the confirmation keeps the form`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent {
            EventFormView(
                state = anEventFormState(form = aNewEventForm(title = "Sprint"), eventSink = eventsRecorder),
                modifier = Modifier,
            )
        }
        onNode(hasContentDescription(activity!!.getString(CommonStrings.action_close))).performClick()
        clickOn(CommonStrings.action_cancel, inDialog = true)
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `the all day option generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        onNode(isToggleable()).performClick()
        eventsRecorder.assertSingle(EventFormEvent.SetAllDay(true))
    }

    @Test
    fun `the time of an all day event is not editable`() = runAndroidComposeUiTest<ComponentActivity> {
        val form = aNewEventForm(title = "Sprint", isAllDay = true)
        setContent { EventFormView(state = anEventFormState(form = form), modifier = Modifier) }
        // There is one time in the start and in the end rows of a timed event, none for an all day event.
        onNode(hasText("11:00 AM")).assertDoesNotExist()
    }

    @Test
    fun `choosing a recurrence generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_repeat)) and hasClickAction()).performClick()
        clickOn(R.string.screen_calendar_repeat_weekly)
        eventsRecorder.assertSingle(EventFormEvent.SetRecurrence(EventRecurrence.Weekly))
    }

    @Test
    fun `adding people generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent { EventFormView(state = anEventFormState(eventSink = eventsRecorder), modifier = Modifier) }
        // The row sits below the fold of the test viewport, so it has to be scrolled to before it can be tapped.
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_add_people)) and hasClickAction())
            .performScrollTo()
            .performClick()
        eventsRecorder.assertSingle(EventFormEvent.OpenParticipantPicker)
    }

    @Test
    fun `editing a single occurrence cannot change the repeat rule or the people`() = runAndroidComposeUiTest<ComponentActivity> {
        val state = anEventFormState(
            form = aNewEventForm(title = "Sprint"),
            mode = EventFormMode.EditOccurrence(RoomId("!event:example.org"), LocalDateTime.of(2026, 9, 24, 10, 0)),
        )
        setContent { EventFormView(state = state, modifier = Modifier) }
        assertNodeWithTextIsDisplayed(R.string.screen_calendar_edit_event)
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_repeat)) and hasClickAction()).assertIsNotEnabled()
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_add_people)) and hasClickAction())
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun `editing the series can change the repeat rule and the people`() = runAndroidComposeUiTest<ComponentActivity> {
        val state = anEventFormState(
            form = aNewEventForm(title = "Sprint"),
            mode = EventFormMode.EditSeries(RoomId("!event:example.org")),
        )
        setContent { EventFormView(state = state, modifier = Modifier) }
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_repeat)) and hasClickAction()).assertIsEnabled()
        onNode(hasText(activity!!.getString(R.string.screen_calendar_event_add_people)) and hasClickAction())
            .performScrollTo()
            .assertIsEnabled()
    }

    @Test
    fun `an error is displayed and can be closed`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        val state = anEventFormState(
            form = aNewEventForm(title = "Sprint"),
            saveAction = AsyncAction.Failure(MeetingsBotUnavailableException()),
            eventSink = eventsRecorder,
        )
        setContent { EventFormView(state = state, modifier = Modifier) }
        assertNodeWithTextIsDisplayed(R.string.screen_calendar_event_error_bot_unavailable)
        clickOn(CommonStrings.action_ok, inDialog = true)
        eventsRecorder.assertSingle(EventFormEvent.ClearSaveError)
    }

    @Test
    fun `the participant picker lists the results and toggles a participant`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        val state = anEventFormState(withParticipantPicker = true, eventSink = eventsRecorder)
        val first = state.participantPicker!!.results.first().matrixUser
        setContent { EventFormView(state = state, modifier = Modifier) }
        onNode(hasText(first.displayName!!) and isToggleable()).performClick()
        eventsRecorder.assertSingle(EventFormEvent.ToggleParticipant(first))
    }

    @Test
    fun `the participant picker can be closed`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<EventFormEvent>()
        setContent {
            EventFormView(state = anEventFormState(withParticipantPicker = true, eventSink = eventsRecorder), modifier = Modifier)
        }
        clickOn(CommonStrings.action_done)
        eventsRecorder.assertList(listOf(EventFormEvent.CloseParticipantPicker))
    }

    @Test
    fun `selected participants are displayed on the form`() = runAndroidComposeUiTest<ComponentActivity> {
        val user = aMatrixUser(id = "@alice:server.org", displayName = "Alice Selected")
        setContent {
            EventFormView(state = anEventFormState(form = aNewEventForm(participants = listOf(user))), modifier = Modifier)
        }
        onNode(hasText("Alice Selected"), useUnmergedTree = true).assertExists()
    }
}
