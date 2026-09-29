/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarState
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class HomeViewTest : RobolectricTest() {
    @Test
    fun `the calendar tab is available in the navigation bar`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<HomeEvent>()
        setHomeContent(aHomeState(eventSink = eventsRecorder))
        onNode(hasContentDescription(activity!!.getString(R.string.screen_home_tab_calendar))).performClick()
        eventsRecorder.assertSingle(HomeEvent.SelectHomeNavigationBarItem(HomeNavigationBarItem.Calendar))
    }

    @Test
    fun `selecting the calendar tab again goes back to today`() = runAndroidComposeUiTest<ComponentActivity> {
        val calendarEvents = EventsRecorder<CalendarEvent>()
        val homeEvents = EventsRecorder<HomeEvent>(expectEvents = false)
        setHomeContent(
            aHomeState(
                currentHomeNavigationBarItem = HomeNavigationBarItem.Calendar,
                calendarState = aCalendarState(eventSink = calendarEvents),
                eventSink = homeEvents,
            )
        )
        onNode(hasContentDescription(activity!!.getString(R.string.screen_home_tab_calendar))).performClick()
        calendarEvents.assertSingle(CalendarEvent.GoToToday)
    }

    @Test
    fun `the floating action button of the calendar tab creates an event`() = runAndroidComposeUiTest<ComponentActivity> {
        val calendarEvents = EventsRecorder<CalendarEvent>()
        setHomeContent(
            aHomeState(
                currentHomeNavigationBarItem = HomeNavigationBarItem.Calendar,
                calendarState = aCalendarState(eventSink = calendarEvents),
            )
        )
        onNode(hasContentDescription(activity!!.getString(R.string.screen_calendar_new_event))).performClick()
        calendarEvents.assertSingle(CalendarEvent.NewEvent)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setHomeContent(state: HomeState) {
        setContent {
            HomeView(
                homeState = state,
                onRoomClick = { _, _ -> },
                onSettingsClick = {},
                onSetUpRecoveryClick = {},
                onConfirmRecoveryKeyClick = {},
                onStartChatClick = {},
                onCreateSpaceClick = {},
                onRoomSettingsClick = {},
                onMenuActionClick = {},
                onReportRoomClick = {},
                onDeclineInviteAndBlockUser = {},
                acceptDeclineInviteView = {},
                leaveRoomView = {},
            )
        }
    }
}
