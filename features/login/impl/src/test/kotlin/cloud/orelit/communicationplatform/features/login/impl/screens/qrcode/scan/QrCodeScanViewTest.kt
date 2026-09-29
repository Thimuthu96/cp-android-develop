/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.scan

import androidx.activity.ComponentActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class QrCodeScanViewTest : RobolectricTest() {
    private var provider: ProcessCameraProvider? = null

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context
        provider = ProcessCameraProvider.getInstance(context).get()
    }

    @After
    fun teardown() {
        provider?.unbindAll()
    }

    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeScanView(
                state = aQrCodeScanState(),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `on QR code data ready - calls the expected callback`() = runAndroidComposeUiTest {
        val data = FakeMatrixQrCodeLoginData()
        ensureCalledOnceWithParam<MatrixQrCodeLoginData>(data) { callback ->
            setQrCodeScanView(
                state = aQrCodeScanState(authenticationAction = AsyncAction.Success(data)),
                onQrCodeDataReady = callback
            )
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeScanView(
        state: QrCodeScanState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onQrCodeDataReady: (MatrixQrCodeLoginData) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            QrCodeScanView(
                state = state,
                onBackClick = onBackClick,
                onQrCodeDataReady = onQrCodeDataReady
            )
        }
    }
}
