/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.confirmation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class QrCodeConfirmationViewTest : RobolectricTest() {
    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeConfirmationView(
                step = QrCodeConfirmationStep.DisplayCheckCode("12"),
                onCancel = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `on Cancel button clicked - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeConfirmationView(
                step = QrCodeConfirmationStep.DisplayVerificationCode("123456"),
                onCancel = callback
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeConfirmationView(
        step: QrCodeConfirmationStep,
        onCancel: () -> Unit
    ) {
        setContent {
            QrCodeConfirmationView(
                step = step,
                onCancel = onCancel
            )
        }
    }
}
