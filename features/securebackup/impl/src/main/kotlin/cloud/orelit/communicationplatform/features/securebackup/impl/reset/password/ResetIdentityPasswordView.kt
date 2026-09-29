/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.reset.password

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.securebackup.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.FlowStepPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.ProgressDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.form.textFieldState
import cloud.orelit.communicationplatform.libraries.designsystem.modifiers.onTabOrEnterKeyFocusNext
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.PasswordVisibilityToggle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextField
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextFieldValidity
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun ResetIdentityPasswordView(
    state: ResetIdentityPasswordState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val passwordState = textFieldState(stateValue = "")
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.Default(CompoundIcons.LockSolid()),
        title = stringResource(R.string.screen_reset_encryption_password_title),
        subTitle = stringResource(R.string.screen_reset_encryption_password_subtitle),
        onBackClick = onBack,
        content = {
            Content(
                text = passwordState.value,
                onTextChange = { newText ->
                    if (state.resetAction.isFailure()) {
                        state.eventSink(ResetIdentityPasswordEvent.DismissError)
                    }
                    passwordState.value = newText
                },
                hasError = state.resetAction.isFailure(),
            )
        },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(CommonStrings.action_reset_identity),
                onClick = { state.eventSink(ResetIdentityPasswordEvent.Reset(passwordState.value)) },
                destructive = true,
                enabled = passwordState.value.isNotEmpty(),
            )
        }
    )

    // On success we need to wait until the screen is automatically dismissed, so we keep the progress dialog
    if (state.resetAction.isLoading() || state.resetAction.isSuccess()) {
        ProgressDialog()
    }
}

@Composable
private fun Content(text: String, onTextChange: (String) -> Unit, hasError: Boolean) {
    var showPassword by remember { mutableStateOf(false) }
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .onTabOrEnterKeyFocusNext(LocalFocusManager.current),
        value = text,
        onValueChange = onTextChange,
        placeholder = stringResource(CommonStrings.common_password),
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            PasswordVisibilityToggle(
                visible = showPassword,
                onToggle = { showPassword = !showPassword },
            )
        },
        validity = if (hasError) TextFieldValidity.Invalid else TextFieldValidity.None,
        supportingText = if (hasError) {
            stringResource(R.string.screen_reset_encryption_password_error)
        } else {
            null
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ResetIdentityPasswordViewPreview(@PreviewParameter(ResetIdentityPasswordStatePreviewParam::class) state: ResetIdentityPasswordState) {
    ElementPreview {
        ResetIdentityPasswordView(
            state = state,
            onBack = {}
        )
    }
}
