/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.securityandprivacy.impl.editroomaddress

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.address.RoomAddressValidity
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class EditRoomAddressViewTest : RobolectricTest() {
    @Test
    fun `click on back invokes expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setEditRoomAddressView(onBackClick = callback)
            pressBack()
        }
    }

    @Test
    fun `click on disabled save doesn't emit event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<EditRoomAddressEvent>(expectEvents = false)
        val state = anEditRoomAddressState(eventSink = recorder)
        setEditRoomAddressView(state)
        clickOn(CommonStrings.action_save)
        recorder.assertEmpty()
    }

    @Test
    fun `click on enabled save emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<EditRoomAddressEvent>()
        val state = anEditRoomAddressState(
            roomAddress = "room",
            roomAddressValidity = RoomAddressValidity.Valid,
            eventSink = recorder
        )
        setEditRoomAddressView(state)
        clickOn(CommonStrings.action_save)
        recorder.assertSingle(EditRoomAddressEvent.Save)
    }

    @Test
    fun `text changes on text field emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<EditRoomAddressEvent>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            eventSink = recorder
        )
        setEditRoomAddressView(state)

        onNodeWithTag(TestTags.roomAddressField.value).performTextInput("alias")
        recorder.assertSingle(EditRoomAddressEvent.RoomAddressChanged("alias"))
    }

    @Test
    fun `click on dismiss error emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<EditRoomAddressEvent>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            saveAction = AsyncAction.Failure(IllegalStateException()),
            eventSink = recorder
        )
        setEditRoomAddressView(state)
        clickOn(CommonStrings.action_cancel)
        recorder.assertSingle(EditRoomAddressEvent.DismissError)
    }

    @Test
    fun `click on retry error emits the expected event`() = runAndroidComposeUiTest {
        val recorder = EventsRecorder<EditRoomAddressEvent>()
        val state = anEditRoomAddressState(
            roomAddress = "",
            saveAction = AsyncAction.Failure(IllegalStateException()),
            eventSink = recorder
        )
        setEditRoomAddressView(state)
        clickOn(CommonStrings.action_retry)
        recorder.assertSingle(EditRoomAddressEvent.Save)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setEditRoomAddressView(
    state: EditRoomAddressState = anEditRoomAddressState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        EditRoomAddressView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
