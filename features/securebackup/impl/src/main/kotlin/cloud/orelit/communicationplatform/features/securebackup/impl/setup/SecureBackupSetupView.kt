/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.setup

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.securebackup.impl.R
import cloud.orelit.communicationplatform.features.securebackup.impl.setup.views.RecoveryKeyView
import cloud.orelit.communicationplatform.libraries.androidutils.system.copyToClipboard
import cloud.orelit.communicationplatform.libraries.androidutils.system.startSharePlainTextIntent
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.FlowStepPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.OutlinedButton
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun SecureBackupSetupView(
    state: SecureBackupSetupState,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        onBackClick = onBackClick.takeIf { state.canGoBack() },
        title = title(state),
        subTitle = subtitle(state),
        iconStyle = BigIcon.Style.Default(CompoundIcons.KeySolid()),
        buttons = { Buttons(state, onFinish = onSuccess) },
    ) {
        Content(state = state)
    }

    if (state.setupState is SetupState.Error) {
        ErrorDialog(
            title = stringResource(id = CommonStrings.common_something_went_wrong),
            content = stringResource(id = CommonStrings.common_something_went_wrong_message),
            onSubmit = {
                state.eventSink.invoke(SecureBackupSetupEvent.DismissDialog)
            },
        )
    }

    if (state.showSaveConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_recovery_key_setup_confirmation_title),
            content = stringResource(id = R.string.screen_recovery_key_setup_confirmation_description),
            submitText = stringResource(id = CommonStrings.action_continue),
            onSubmitClick = onSuccess,
            onDismiss = {
                state.eventSink.invoke(SecureBackupSetupEvent.DismissDialog)
            }
        )
    }
}

private fun SecureBackupSetupState.canGoBack(): Boolean {
    return recoveryKeyViewState.formattedRecoveryKey == null
}

@Composable
private fun title(state: SecureBackupSetupState): String {
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> if (state.isChangeRecoveryKeyUserStory) {
            stringResource(id = R.string.screen_recovery_key_change_title)
        } else {
            stringResource(id = R.string.screen_recovery_key_setup_title)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_title)
    }
}

@Composable
private fun subtitle(state: SecureBackupSetupState): String {
    return when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> if (state.isChangeRecoveryKeyUserStory) {
            stringResource(id = R.string.screen_recovery_key_change_description)
        } else {
            stringResource(id = R.string.screen_recovery_key_setup_description)
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved ->
            stringResource(id = R.string.screen_recovery_key_save_description)
    }
}

@Composable
private fun Content(
    state: SecureBackupSetupState,
) {
    val context = LocalContext.current
    val formattedRecoveryKey = state.recoveryKeyViewState.formattedRecoveryKey
    val toastMessage = stringResource(R.string.screen_recovery_key_copied_to_clipboard)
    val clickLambda = if (formattedRecoveryKey != null) {
        {
            context.copyToClipboard(
                text = formattedRecoveryKey,
                toastMessage = toastMessage,
            )
            state.eventSink.invoke(SecureBackupSetupEvent.RecoveryKeyHasBeenSaved)
        }
    } else {
        if (!state.recoveryKeyViewState.inProgress) {
            {
                state.eventSink.invoke(SecureBackupSetupEvent.CreateRecoveryKey)
            }
        } else {
            null
        }
    }
    RecoveryKeyView(
        modifier = Modifier.padding(top = 52.dp),
        state = state.recoveryKeyViewState,
        onClick = clickLambda,
        onChange = null,
        onSubmit = null,
        toggleRecoveryKeyVisibility = {},
    )
}

@Composable
private fun ColumnScope.Buttons(
    state: SecureBackupSetupState,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(id = R.string.screen_recovery_key_save_action)
    when (state.setupState) {
        SetupState.Init,
        SetupState.Creating,
        is SetupState.Error -> {
            Button(
                text = stringResource(id = CommonStrings.action_done),
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                onClick = onFinish
            )
        }
        is SetupState.Created,
        is SetupState.CreatedAndSaved -> {
            OutlinedButton(
                text = stringResource(id = R.string.screen_recovery_key_save_action),
                leadingIcon = IconSource.Vector(CompoundIcons.Download()),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = chooserTitle,
                        text = state.setupState.recoveryKey()!!,
                    )
                    state.eventSink.invoke(SecureBackupSetupEvent.RecoveryKeyHasBeenSaved)
                },
            )
            Button(
                text = stringResource(id = CommonStrings.action_done),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (state.setupState is SetupState.CreatedAndSaved) {
                        onFinish()
                    } else {
                        state.eventSink.invoke(SecureBackupSetupEvent.Done)
                    }
                },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SecureBackupSetupViewPreview(
    @PreviewParameter(SecureBackupSetupStatePreviewParam::class) state: SecureBackupSetupState
) = ElementPreview {
    SecureBackupSetupView(
        state = state,
        onSuccess = {},
        onBackClick = {},
    )
}
