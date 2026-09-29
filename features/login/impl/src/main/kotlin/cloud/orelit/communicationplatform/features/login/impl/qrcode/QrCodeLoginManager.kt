/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.qrcode

import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.StateFlow

/**
 * Helper to handle the QR code login flow after the QR code data has been provided.
 */
interface QrCodeLoginManager {
    /**
     * The current QR code login step.
     */
    val currentLoginStep: StateFlow<QrCodeLoginStep>

    /**
     * Authenticate using the provided [qrCodeLoginData].
     * @param qrCodeLoginData the QR code login data from the scanned QR code.
     * @return the logged in [SessionId] if the authentication was successful or a failure result.
     */
    suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId>

    fun reset()
}
