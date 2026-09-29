/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.intro

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsState
import cloud.orelit.communicationplatform.libraries.permissions.api.aPermissionsState

open class QrCodeIntroStatePreviewParam : PreviewParameterProvider<QrCodeIntroState> {
    override val values: Sequence<QrCodeIntroState>
        get() = sequenceOf(
            aQrCodeIntroState(),
            aQrCodeIntroState(cameraPermissionState = aPermissionsState(showDialog = true, permission = Manifest.permission.CAMERA)),
            // Add other state here
        )
}

fun aQrCodeIntroState(
    appName: String = "AppName",
    desktopAppName: String = "Element",
    cameraPermissionState: PermissionsState = aPermissionsState(
        showDialog = false,
        permission = Manifest.permission.CAMERA,
    ),
    canContinue: Boolean = false,
    eventSink: (QrCodeIntroEvent) -> Unit = {},
) = QrCodeIntroState(
    appName = appName,
    desktopAppName = desktopAppName,
    cameraPermissionState = cameraPermissionState,
    canContinue = canContinue,
    eventSink = eventSink
)
