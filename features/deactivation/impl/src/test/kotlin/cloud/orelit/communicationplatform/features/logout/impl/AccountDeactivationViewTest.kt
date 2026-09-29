/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.logout.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.deactivation.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_PASSWORD
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.pressTag
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class AccountDeactivationViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>(expectEvents = false)
        ensureCalledOnce {
            setAccountDeactivationView(
                state = anAccountDeactivationState(eventSink = eventsRecorder),
                onBackClick = it,
            )
            pressBack()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Deactivate emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_delete)
        eventsRecorder.assertSingle(AccountDeactivationEvent.DeactivateAccount(false))
    }

    @Test
    fun `clicking on Deactivate on the confirmation dialog emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                accountDeactivationAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder,
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(AccountDeactivationEvent.DeactivateAccount(false))
    }

    @Test
    fun `clicking on retry on the confirmation dialog emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                accountDeactivationAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(AccountDeactivationEvent.DeactivateAccount(true))
    }

    @Test
    fun `switching on the erase all switch emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_deactivate_account_delete_all_messages)
        eventsRecorder.assertSingle(AccountDeactivationEvent.SetEraseData(true))
    }

    @Test
    fun `switching off the erase all switch emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    eraseData = true,
                ),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(R.string.screen_deactivate_account_delete_all_messages)
        eventsRecorder.assertSingle(AccountDeactivationEvent.SetEraseData(false))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `typing text in the password field emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<AccountDeactivationEvent>()
        setAccountDeactivationView(
            state = anAccountDeactivationState(
                deactivateFormState = aDeactivateFormState(
                    password = A_PASSWORD,
                ),
                eventSink = eventsRecorder,
            ),
        )
        onNodeWithTag(TestTags.loginPassword.value).performTextInput("A")
        eventsRecorder.assertSingle(AccountDeactivationEvent.SetPassword("A$A_PASSWORD"))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setAccountDeactivationView(
    state: AccountDeactivationState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AccountDeactivationView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
