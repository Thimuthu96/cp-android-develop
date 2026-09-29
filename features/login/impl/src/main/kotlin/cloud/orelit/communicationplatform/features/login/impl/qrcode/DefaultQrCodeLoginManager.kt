/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.qrcode

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.features.login.impl.di.QrCodeLoginScope
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.MatrixAuthenticationService
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.QrLoginException
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SingleIn(QrCodeLoginScope::class)
@ContributesBinding(QrCodeLoginScope::class)
class DefaultQrCodeLoginManager(
    private val authenticationService: MatrixAuthenticationService,
) : QrCodeLoginManager {
    private val _currentLoginStep = MutableStateFlow<QrCodeLoginStep>(QrCodeLoginStep.Uninitialized)
    override val currentLoginStep: StateFlow<QrCodeLoginStep> = _currentLoginStep

    override suspend fun authenticate(qrCodeLoginData: MatrixQrCodeLoginData): Result<SessionId> {
        reset()

        return authenticationService.loginWithQrCode(qrCodeLoginData) { step ->
            _currentLoginStep.value = step
        }.onFailure { throwable ->
            if (throwable is QrLoginException) {
                _currentLoginStep.value = QrCodeLoginStep.Failed(throwable)
            }
        }
    }

    override fun reset() {
        _currentLoginStep.value = QrCodeLoginStep.Uninitialized
    }
}
