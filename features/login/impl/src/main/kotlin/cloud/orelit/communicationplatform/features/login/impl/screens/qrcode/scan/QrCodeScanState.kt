/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.scan

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData

data class QrCodeScanState(
    val isScanning: Boolean,
    val authenticationAction: AsyncAction<MatrixQrCodeLoginData>,
    val eventSink: (QrCodeScanEvent) -> Unit
)
