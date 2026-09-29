/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ModalBottomSheet
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.OutlinedButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolveVerifiedUserSendFailureView(
    state: ResolveVerifiedUserSendFailureState,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    var showSheet by remember { mutableStateOf(false) }

    fun dismiss() {
        state.eventSink(ResolveVerifiedUserSendFailureEvent.Dismiss)
    }

    fun onRetryClick() {
        state.eventSink(ResolveVerifiedUserSendFailureEvent.Retry)
    }

    fun onResolveAndResendClick() {
        state.eventSink(ResolveVerifiedUserSendFailureEvent.ResolveAndResend)
    }

    LaunchedEffect(state.verifiedUserSendFailure) {
        if (state.verifiedUserSendFailure is VerifiedUserSendFailure.None) {
            sheetState.hide()
            showSheet = false
        } else {
            showSheet = true
        }
    }

    Box(modifier = modifier) {
        if (showSheet) {
            ModalBottomSheet(
                modifier = Modifier
                    .systemBarsPadding()
                    .navigationBarsPadding(),
                sheetState = sheetState,
                onDismissRequest = ::dismiss,
                scrollable = true,
            ) {
                IconTitleSubtitleMolecule(
                    modifier = Modifier.padding(24.dp),
                    title = state.verifiedUserSendFailure.title(),
                    subTitle = state.verifiedUserSendFailure.subtitle(),
                    iconStyle = BigIcon.Style.AlertSolid,
                )
                ButtonColumnMolecule(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = state.verifiedUserSendFailure.resolveAction(),
                        showProgress = state.resolveAction.isLoading(),
                        onClick = ::onResolveAndResendClick
                    )
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = CommonStrings.action_retry),
                        showProgress = state.retryAction.isLoading(),
                        onClick = ::onRetryClick
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = CommonStrings.action_cancel_for_now),
                        onClick = ::dismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifiedUserSendFailure.title(): String {
    return when (this) {
        is VerifiedUserSendFailure.UnsignedDevice.FromOther -> stringResource(
            id = CommonStrings.screen_resolve_send_failure_unsigned_device_title,
            userDisplayName
        )
        VerifiedUserSendFailure.UnsignedDevice.FromYou -> stringResource(id = CommonStrings.screen_resolve_send_failure_you_unsigned_device_title)
        is VerifiedUserSendFailure.ChangedIdentity -> stringResource(
            id = CommonStrings.screen_resolve_send_failure_changed_identity_title,
            userDisplayName
        )
        VerifiedUserSendFailure.None -> ""
    }
}

@Composable
private fun VerifiedUserSendFailure.subtitle(): String {
    return when (this) {
        is VerifiedUserSendFailure.UnsignedDevice.FromOther -> stringResource(
            id = CommonStrings.screen_resolve_send_failure_unsigned_device_subtitle,
            userDisplayName,
            userDisplayName,
        )
        VerifiedUserSendFailure.UnsignedDevice.FromYou -> stringResource(id = CommonStrings.screen_resolve_send_failure_you_unsigned_device_subtitle)
        is VerifiedUserSendFailure.ChangedIdentity -> stringResource(
            id = CommonStrings.screen_resolve_send_failure_changed_identity_subtitle,
            userDisplayName
        )
        VerifiedUserSendFailure.None -> ""
    }
}

@Composable
private fun VerifiedUserSendFailure.resolveAction(): String {
    return when (this) {
        is VerifiedUserSendFailure.UnsignedDevice -> stringResource(id = CommonStrings.screen_resolve_send_failure_unsigned_device_primary_button_title)
        is VerifiedUserSendFailure.ChangedIdentity -> stringResource(id = CommonStrings.screen_resolve_send_failure_changed_identity_primary_button_title)
        VerifiedUserSendFailure.None -> ""
    }
}

@PreviewsDayNight
@Composable
internal fun ResolveVerifiedUserSendFailureViewPreview(
    @PreviewParameter(ResolveVerifiedUserSendFailureStatePreviewParam::class) state: ResolveVerifiedUserSendFailureState
) = ElementPreview {
    ResolveVerifiedUserSendFailureView(state)
}
