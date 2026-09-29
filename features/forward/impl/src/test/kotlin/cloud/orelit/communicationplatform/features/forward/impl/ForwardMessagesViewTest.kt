/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.forward.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.pressTag
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ForwardMessagesViewTest : RobolectricTest() {
    @Test
    fun `cancel error emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ForwardMessagesEvent>()
        setForwardMessagesView(
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(ForwardMessagesEvent.ClearError)
    }

    @Test
    fun `success invokes onForwardSuccess`() = runAndroidComposeUiTest {
        val data = listOf(A_ROOM_ID)
        val eventsRecorder = EventsRecorder<ForwardMessagesEvent>(expectEvents = false)
        ensureCalledOnceWithParam<List<RoomId>?>(data) { callback ->
            setForwardMessagesView(
                aForwardMessagesState(
                    forwardAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder
                ),
                onForwardSuccess = callback,
            )
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setForwardMessagesView(
    state: ForwardMessagesState,
    onForwardSuccess: (List<RoomId>) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        ForwardMessagesView(
            state = state,
            onForwardSuccess = onForwardSuccess,
        )
    }
}
