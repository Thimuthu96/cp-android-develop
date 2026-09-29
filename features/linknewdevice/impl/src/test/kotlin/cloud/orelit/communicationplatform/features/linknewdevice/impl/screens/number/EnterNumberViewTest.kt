/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.number

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class EnterNumberViewTest : RobolectricTest() {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aEnterNumberState(),
                onBackClicked = callback,
            )
            pressBackKey()
        }
    }

    @Test
    fun `on back button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setView(
                state = aEnterNumberState(),
                onBackClicked = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `on continue button clicked - emits the Continue event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<EnterNumberEvent>()
        setView(
            state = aEnterNumberState(
                number = "12",
                eventSink = eventRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventRecorder.assertSingle(EnterNumberEvent.Continue)
    }

    @Test
    fun `when the number is not complete, continue button is disabled`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<EnterNumberEvent>(expectEvents = false)
        setView(
            state = aEnterNumberState(
                number = "1",
                eventSink = eventRecorder,
            ),
        )
        val continueStr = activity!!.getString(CommonStrings.action_continue)
        onNodeWithText(continueStr).assertIsNotEnabled()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        state: EnterNumberState,
        onBackClicked: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            EnterNumberView(
                state = state,
                onBackClick = onBackClicked,
            )
        }
    }
}
