/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.verifysession.impl.outgoing

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.verifysession.impl.R
import cloud.orelit.communicationplatform.features.verifysession.impl.ui.aEmojisSessionVerificationData
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class OutgoingVerificationViewTest : RobolectricTest() {
    @Test
    fun `back key pressed - when canceled resets the flow`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Canceled,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.Reset)
    }

    @Test
    fun `back key pressed - when awaiting response cancels the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.AwaitingOtherDeviceResponse,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.Cancel)
    }

    @Test
    fun `back key pressed - when ready to verify cancels the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Ready,
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.Cancel)
    }

    @Test
    fun `back key pressed - when verifying and not loading declines the verification`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.DeclineVerification)
    }

    @Test
    fun `back key pressed - when verifying and loading does nothing`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Loading(),
                ),
                eventSink = eventsRecorder
            ),
        )
        pressBackKey()
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `back key pressed - on Completed finishes the flow`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setOutgoingVerificationView(
                onFinished = callback,
                state = anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                ),
            )
            pressBackKey()
        }
    }

    @Test
    fun `when flow is completed and the user clicks on the done button, the expected callback is invoked`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setOutgoingVerificationView(
                anOutgoingVerificationState(
                    step = OutgoingVerificationState.Step.Completed,
                    eventSink = eventsRecorder
                ),
                onFinished = callback,
            )
            clickOn(CommonStrings.action_done)
        }
    }

    @Test
    fun `clicking on they match emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_session_verification_they_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.ConfirmVerification)
    }

    @Test
    fun `clicking on they do not match emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<OutgoingVerificationViewEvent>()
        setOutgoingVerificationView(
            anOutgoingVerificationState(
                step = OutgoingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    state = AsyncData.Uninitialized,
                ),
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_session_verification_they_dont_match)
        eventsRecorder.assertSingle(OutgoingVerificationViewEvent.DeclineVerification)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setOutgoingVerificationView(
        state: OutgoingVerificationState,
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onFinished: () -> Unit = EnsureNeverCalled(),
        onBack: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            OutgoingVerificationView(
                state = state,
                onLearnMoreClick = onLearnMoreClick,
                onFinish = onFinished,
                onBack = onBack,
            )
        }
    }
}
