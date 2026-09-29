/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_FORMATTED_DATE
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PushHistoryViewTest : RobolectricTest() {
    @Test
    fun `clicking on Reset sends a PushHistoryEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvent>()
        setPushHistoryView(
            aPushHistoryState(
                pushCounter = 123,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        clickOn(CommonStrings.action_reset)
        eventsRecorder.assertSingle(PushHistoryEvent.Reset(requiresConfirmation = true))
        // Also check that the push counter is rendered
        onNodeWithText("123").assertExists()
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvent(true)`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvent>()
        setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = false,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvent.SetShowOnlyErrors(showOnlyErrors = true))
    }

    @Test
    fun `clicking on show only errors sends a PushHistoryEvent(false)`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvent>()
        setPushHistoryView(
            aPushHistoryState(
                showOnlyErrors = true,
                eventSink = eventsRecorder,
            ),
        )
        val menuContentDescription = activity!!.getString(CommonStrings.a11y_user_menu)
        onNodeWithContentDescription(menuContentDescription).performClick()
        onNodeWithText("Show only errors").performClick()
        eventsRecorder.assertSingle(PushHistoryEvent.SetShowOnlyErrors(showOnlyErrors = false))
    }

    @Test
    fun `clicking on an invalid event has no effect`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvent>(expectEvents = false)
        setPushHistoryView(
            aPushHistoryState(
                pushHistoryItems = listOf(
                    aPushHistoryItem(
                        formattedDate = A_FORMATTED_DATE,
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(A_FORMATTED_DATE).performClick()
        // No callback invoked
    }

    @Test
    fun `clicking on a valid event emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PushHistoryEvent>()
        setPushHistoryView(
            aPushHistoryState(
                pushHistoryItems = listOf(
                    aPushHistoryItem(
                        formattedDate = A_FORMATTED_DATE,
                        eventId = AN_EVENT_ID,
                        roomId = A_ROOM_ID,
                        sessionId = A_SESSION_ID,
                    )
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithText(A_FORMATTED_DATE).performClick()
        eventsRecorder.assertSingle(
            PushHistoryEvent.NavigateTo(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                eventId = AN_EVENT_ID,
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setPushHistoryView(
    state: PushHistoryState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PushHistoryView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
