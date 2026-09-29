/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.roomdirectory.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class RoomDirectoryViewTest : RobolectricTest() {
    @Test
    fun `typing text in search field emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvent>()
        setRoomDirectoryView(
            state = aRoomDirectoryState(
                eventSink = eventsRecorder,
            )
        )
        onNodeWithTag(TestTags.searchTextField.value).performTextInput(
            text = "Test"
        )
        eventsRecorder.assertSingle(RoomDirectoryEvent.Search("Test"))
    }

    @Test
    fun `clicking on room item then onResultClick lambda is called once`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvent>()
        val state = aRoomDirectoryState(
            roomDescriptions = aRoomDescriptionList(),
            eventSink = eventsRecorder,
        )
        val clickedRoom = state.roomDescriptions.first()
        ensureCalledOnceWithParam(clickedRoom) { callback ->
            setRoomDirectoryView(
                state = state,
                onResultClick = callback,
            )
            onNodeWithText(clickedRoom.computedName).performClick()
        }
    }

    @Test
    fun `composing load more indicator emits expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvent>()
        val state = aRoomDirectoryState(
            displayLoadMoreIndicator = true,
            eventSink = eventsRecorder,
        )
        setRoomDirectoryView(state = state)
        eventsRecorder.assertSingle(RoomDirectoryEvent.LoadMore)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomDirectoryView(
    state: RoomDirectoryState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onResultClick: (RoomDescription) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomDirectoryView(
            state = state,
            onResultClick = onResultClick,
            onBackClick = onBackClick,
        )
    }
}
