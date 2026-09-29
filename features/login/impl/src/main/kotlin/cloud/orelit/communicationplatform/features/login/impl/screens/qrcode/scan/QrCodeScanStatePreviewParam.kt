/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.scan

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.login.impl.changeserver.AccountProviderAccessException
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.QrLoginException

open class QrCodeScanStatePreviewParam : PreviewParameterProvider<QrCodeScanState> {
    override val values: Sequence<QrCodeScanState>
        get() = sequenceOf(
            aQrCodeScanState(),
            aQrCodeScanState(isScanning = false, authenticationAction = AsyncAction.Loading),
            aQrCodeScanState(isScanning = false, authenticationAction = AsyncAction.Failure(Exception("Error"))),
            aQrCodeScanState(isScanning = false, authenticationAction = AsyncAction.Failure(QrLoginException.OtherDeviceNotSignedIn)),
            aQrCodeScanState(
                isScanning = false,
                authenticationAction = AsyncAction.Failure(
                    AccountProviderAccessException.UnauthorizedAccountProviderException(
                        unauthorisedAccountProviderTitle = "example.com",
                        authorisedAccountProviderTitles = listOf("element.io", "element.org"),
                    )
                )
            ),
            aQrCodeScanState(
                isScanning = false,
                authenticationAction = AsyncAction.Failure(
                    AccountProviderAccessException.NeedElementProException(
                        unauthorisedAccountProviderTitle = "example.com",
                        applicationId = "applicationId"
                    )
                )
            ),
            // Add other state here
        )
}

fun aQrCodeScanState(
    isScanning: Boolean = true,
    authenticationAction: AsyncAction<MatrixQrCodeLoginData> = AsyncAction.Uninitialized,
    eventSink: (QrCodeScanEvent) -> Unit = {},
) = QrCodeScanState(
    isScanning = isScanning,
    authenticationAction = authenticationAction,
    eventSink = eventSink
)
