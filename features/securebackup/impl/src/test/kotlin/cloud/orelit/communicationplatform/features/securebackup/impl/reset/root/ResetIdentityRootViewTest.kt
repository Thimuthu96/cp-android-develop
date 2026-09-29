/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.securebackup.impl.reset.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.securebackup.impl.R
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ResetIdentityRootViewTest : RobolectricTest() {
    @Test
    fun `pressing the back HW button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = false, eventSink = {}),
                onBack = it,
            )
            pressBackKey()
        }
    }

    @Test
    fun `clicking on the back navigation button invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = false, eventSink = {}),
                onBack = it,
            )
            pressBack()
        }
    }

    @Test
    @Config(qualifiers = "h720dp")
    fun `clicking Continue displays the confirmation dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        setResetRootView(
            ResetIdentityRootState(displayConfirmationDialog = false, eventSink = eventsRecorder),
        )

        clickOn(R.string.screen_encryption_reset_action_continue_reset)

        eventsRecorder.assertSingle(ResetIdentityRootEvent.Continue)
    }

    @Test
    fun `clicking 'Yes, reset now' confirms the reset`() = runAndroidComposeUiTest {
        ensureCalledOnce {
            setResetRootView(
                ResetIdentityRootState(displayConfirmationDialog = true, eventSink = {}),
                onContinue = it,
            )
            clickOn(R.string.screen_reset_encryption_confirmation_alert_action)
        }
    }

    @Test
    fun `clicking Cancel dismisses the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResetIdentityRootEvent>()
        setResetRootView(
            ResetIdentityRootState(displayConfirmationDialog = true, eventSink = eventsRecorder),
        )

        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ResetIdentityRootEvent.DismissDialog)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setResetRootView(
    state: ResetIdentityRootState,
    onBack: () -> Unit = EnsureNeverCalled(),
    onContinue: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ResetIdentityRootView(state = state, onContinue = onContinue, onBack = onBack)
    }
}
