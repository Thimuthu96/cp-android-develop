/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.scan

import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ScanQrCodeViewTest : RobolectricTest() {
    @Test
    @Config(sdk = [UPSIDE_DOWN_CAKE])
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<ScanQrCodeEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setView(
                state = aScanQrCodeState(
                    eventSink = eventRecorder,
                ),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    @Config(sdk = [UPSIDE_DOWN_CAKE])
    fun `try again button clicked - emits the expected event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<ScanQrCodeEvent>()
        setView(
            state = aScanQrCodeState(
                scanAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventRecorder,
            )
        )
        clickOn(CommonStrings.action_try_again)
        eventRecorder.assertSingle(ScanQrCodeEvent.TryAgain)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setView(
        state: ScanQrCodeState = aScanQrCodeState(),
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                ScanQrCodeView(
                    state = state,
                    onBackClick = onBackClick,
                )
            }
        }
    }
}
