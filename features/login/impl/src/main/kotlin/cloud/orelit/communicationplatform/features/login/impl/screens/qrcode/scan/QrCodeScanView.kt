/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.qrcode.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.login.impl.R
import cloud.orelit.communicationplatform.features.login.impl.changeserver.AccountProviderAccessException
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.FlowStepPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.modifiers.cornerBorder
import cloud.orelit.communicationplatform.libraries.designsystem.modifiers.squareSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.QrLoginException
import cloud.orelit.communicationplatform.libraries.qrcode.QrCodeCameraView

@Composable
fun QrCodeScanView(
    state: QrCodeScanState,
    onBackClick: () -> Unit,
    onQrCodeDataReady: (MatrixQrCodeLoginData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedOnQrCodeDataReady by rememberUpdatedState(onQrCodeDataReady)
    // QR code data parsed successfully, notify the parent node
    if (state.authenticationAction is AsyncAction.Success) {
        LaunchedEffect(state.authenticationAction, updatedOnQrCodeDataReady) {
            updatedOnQrCodeDataReady(state.authenticationAction.data)
        }
    }

    FlowStepPage(
        modifier = modifier,
        onBackClick = onBackClick,
        iconStyle = BigIcon.Style.Default(CompoundIcons.Computer()),
        title = stringResource(R.string.screen_qr_code_login_scanning_state_title),
        content = { Content(state = state) },
        buttons = { Buttons(state = state) }
    )
}

@Composable
private fun Content(
    state: QrCodeScanState,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val modifier = if (constraints.maxWidth > constraints.maxHeight) {
            Modifier.fillMaxHeight()
        } else {
            Modifier.fillMaxWidth()
        }.then(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 50.dp, bottom = 32.dp)
                .squareSize()
                .cornerBorder(
                    strokeWidth = 4.dp,
                    color = ElementTheme.colors.textPrimary,
                    cornerSizeDp = 42.dp,
                )
        )
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            QrCodeCameraView(
                modifier = Modifier.fillMaxSize(),
                onScanQrCode = { state.eventSink.invoke(QrCodeScanEvent.QrCodeScanned(it)) },
                isScanning = state.isScanning,
            )
        }
    }
}

@Composable
private fun ColumnScope.Buttons(
    state: QrCodeScanState,
) {
    Column(Modifier.heightIn(min = 130.dp)) {
        when (state.authenticationAction) {
            is AsyncAction.Failure -> {
                Button(
                    text = stringResource(id = R.string.screen_qr_code_login_invalid_scan_state_retry_button),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    onClick = {
                        state.eventSink.invoke(QrCodeScanEvent.TryAgain)
                    }
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val error = state.authenticationAction.error
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = CompoundIcons.ErrorSolid(),
                            tint = ElementTheme.colors.iconCriticalPrimary,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (error) {
                                is AccountProviderAccessException.NeedElementProException -> {
                                    stringResource(R.string.screen_change_server_error_element_pro_required_title)
                                }
                                is AccountProviderAccessException.UnauthorizedAccountProviderException -> {
                                    stringResource(
                                        id = R.string.screen_change_server_error_unauthorized_homeserver_title,
                                        error.unauthorisedAccountProviderTitle,
                                    )
                                }
                                is QrLoginException.OtherDeviceNotSignedIn -> {
                                    stringResource(R.string.screen_qr_code_login_device_not_signed_in_scan_state_subtitle)
                                }
                                else -> stringResource(R.string.screen_qr_code_login_invalid_scan_state_subtitle)
                            },
                            textAlign = TextAlign.Center,
                            color = ElementTheme.colors.textCriticalPrimary,
                            style = ElementTheme.typography.fontBodySmMedium,
                        )
                    }
                    Text(
                        text = when (error) {
                            is AccountProviderAccessException.NeedElementProException -> {
                                stringResource(
                                    R.string.screen_change_server_error_element_pro_required_message,
                                    error.unauthorisedAccountProviderTitle,
                                )
                            }
                            is AccountProviderAccessException.UnauthorizedAccountProviderException -> {
                                stringResource(
                                    id = R.string.screen_change_server_error_unauthorized_homeserver_content,
                                    error.authorisedAccountProviderTitles.joinToString(),
                                )
                            }
                            is QrLoginException.OtherDeviceNotSignedIn -> {
                                stringResource(R.string.screen_qr_code_login_device_not_signed_in_scan_state_description)
                            }
                            else -> stringResource(R.string.screen_qr_code_login_invalid_scan_state_description)
                        },
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
            AsyncAction.Loading, is AsyncAction.Success -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .progressSemantics()
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.screen_qr_code_login_connecting_subtitle),
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
            AsyncAction.Uninitialized,
            is AsyncAction.Confirming -> Unit
        }
    }
}

@PreviewsDayNight
@Composable
internal fun QrCodeScanViewPreview(@PreviewParameter(QrCodeScanStatePreviewParam::class) state: QrCodeScanState) = ElementPreview {
    QrCodeScanView(
        state = state,
        onQrCodeDataReady = {},
        onBackClick = {},
    )
}
