/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.securebackup.impl.reset.password

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ResetIdentityPasswordViewTest : RobolectricTest() {
    @Test
    fun `pressing the back HW button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetPasswordView(
                ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = {}),
                onBack = it,
            )
            pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetPasswordView(
                ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = {}),
                onBack = it,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking 'Reset identity' confirms the reset`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Uninitialized, eventSink = eventsRecorder),
        )
        onNodeWithText("Password").performTextInput("A password")

        clickOn(CommonStrings.action_reset_identity)

        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.Reset("A password"))
    }

    @Test
    fun `modifying the password dismisses the error state`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityPasswordEvent>()
        setResetPasswordView(
            ResetIdentityPasswordState(resetAction = AsyncAction.Failure(IllegalStateException("A failure")), eventSink = eventsRecorder),
        )
        onNodeWithText("Password").performTextInput("A password")

        eventsRecorder.assertSingle(ResetIdentityPasswordEvent.DismissError)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setResetPasswordView(
    state: ResetIdentityPasswordState,
    onBack: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityPasswordView(state = state, onBack = onBack)
    }
}
