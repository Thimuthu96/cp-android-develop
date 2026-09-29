/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class CalendarViewTest : RobolectricTest() {
    private val today = A_CALENDAR_NOW.toLocalDate()

    @Test
    fun `clicking on today generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<CalendarEvent>()
        setContent {
            Row { CalendarTopBarActions(state = aCalendarState(eventSink = eventsRecorder)) }
        }
        onNode(hasContentDescription(activity!!.getString(R.string.screen_calendar_today))).performClick()
        eventsRecorder.assertSingle(CalendarEvent.GoToToday)
    }

    @Test
    fun `choosing a view mode generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<CalendarEvent>()
        setContent {
            Row { CalendarTopBarActions(state = aCalendarState(eventSink = eventsRecorder)) }
        }
        onNode(hasContentDescription(activity!!.getString(R.string.screen_calendar_change_view))).performClick()
        clickOn(R.string.screen_calendar_view_week)
        eventsRecorder.assertSingle(CalendarEvent.SelectViewMode(CalendarViewMode.Week))
    }

    @Test
    fun `the empty agenda displays the empty state`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            CalendarView(
                state = aCalendarState(viewMode = CalendarViewMode.Agenda, entries = emptyList()),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        onNodeWithText(activity!!.getString(R.string.screen_calendar_empty_title)).assertExists()
    }

    @Test
    fun `the agenda displays the entries of the selected day`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            CalendarView(
                state = aCalendarState(viewMode = CalendarViewMode.Agenda),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        onNodeWithText("Daily stand-up").assertExists()
    }

    @Test
    fun `the day view displays the entries of the selected day`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            CalendarView(
                state = aCalendarState(viewMode = CalendarViewMode.Day),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        onNodeWithText("Daily stand-up").assertExists()
        onNodeWithText("Design review").assertExists()
    }

    @Test
    fun `swiping the day view selects the next day`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<CalendarEvent>()
        setContent {
            CalendarView(
                state = aCalendarState(viewMode = CalendarViewMode.Day, eventSink = eventsRecorder),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        // A slow swipe of about half the width moves exactly one page, a fast one would fling over several.
        onRoot().performTouchInput { swipeLeft(startX = right * 0.8f, endX = right * 0.3f, durationMillis = 600) }
        waitForIdle()
        eventsRecorder.assertSingle(CalendarEvent.SelectDate(today.plusDays(1)))
    }

    @Test
    fun `selecting another date scrolls the day view to it`() = runAndroidComposeUiTest<ComponentActivity> {
        var selectedDate by mutableStateOf(today)
        setContent {
            CalendarView(
                state = aCalendarState(viewMode = CalendarViewMode.Day, selectedDate = selectedDate),
                contentPadding = PaddingValues(),
                modifier = Modifier.fillMaxSize(),
            )
        }
        onNodeWithText(today.dayOfMonth.toString()).assertExists()
        selectedDate = today.plusDays(3)
        waitForIdle()
        onNodeWithText(today.plusDays(3).dayOfMonth.toString()).assertExists()
        onNodeWithText(today.dayOfMonth.toString()).assertDoesNotExist()
    }
}
