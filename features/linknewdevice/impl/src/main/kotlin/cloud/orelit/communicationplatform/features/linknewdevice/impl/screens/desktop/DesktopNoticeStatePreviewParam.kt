/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.desktop

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsState
import cloud.orelit.communicationplatform.libraries.permissions.api.aPermissionsState

open class DesktopNoticeStatePreviewParam : PreviewParameterProvider<DesktopNoticeState> {
    override val values: Sequence<DesktopNoticeState>
        get() = sequenceOf(
            aDesktopNoticeState(),
            aDesktopNoticeState(cameraPermissionState = aPermissionsState(showDialog = true, permission = Manifest.permission.CAMERA)),
        )
}

fun aDesktopNoticeState(
    cameraPermissionState: PermissionsState = aPermissionsState(
        showDialog = false,
        permission = Manifest.permission.CAMERA,
    ),
    canContinue: Boolean = false,
    eventSink: (DesktopNoticeEvent) -> Unit = {},
) = DesktopNoticeState(
    cameraPermissionState = cameraPermissionState,
    canContinue = canContinue,
    eventSink = eventSink
)
