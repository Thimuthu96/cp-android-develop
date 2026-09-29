/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.setSafeContent
import org.junit.Test

class ResolveVerifiedUserSendFailureViewTest : RobolectricTest() {
    @Test
    fun `clicking on resolve and resend emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvent>()
        setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        clickOn(res = CommonStrings.screen_resolve_send_failure_changed_identity_primary_button_title)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvent.ResolveAndResend)
    }

    @Test
    fun `clicking on retry emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ResolveVerifiedUserSendFailureEvent>()
        setResolveVerifiedUserSendFailureView(
            state = aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure(),
                eventSink = eventsRecorder,
            ),
        )

        clickOn(res = CommonStrings.action_retry)
        eventsRecorder.assertSingle(ResolveVerifiedUserSendFailureEvent.Retry)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setResolveVerifiedUserSendFailureView(
        state: ResolveVerifiedUserSendFailureState,
    ) {
        setSafeContent {
            ResolveVerifiedUserSendFailureView(state = state)
        }
    }
}
