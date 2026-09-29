/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.lockscreen.impl.R
import cloud.orelit.communicationplatform.features.lockscreen.impl.components.PinEntryTextField
import cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin.validation.SetupPinFailure
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar

@Composable
fun SetupPinView(
    state: SetupPinState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                },
                title = {}
            )
        },
        content = { padding ->
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .imePadding()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .verticalScroll(state = scrollState)
                    .padding(vertical = 16.dp, horizontal = 20.dp),
            ) {
                SetupPinHeader(state.isConfirmationStep, state.appName)
                SetupPinContent(state)
            }
        }
    )
}

@Composable
private fun SetupPinHeader(
    isValidationStep: Boolean,
    appName: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTitleSubtitleMolecule(
            title = if (isValidationStep) {
                stringResource(id = R.string.screen_app_lock_setup_confirm_pin)
            } else {
                stringResource(id = R.string.screen_app_lock_setup_choose_pin)
            },
            subTitle = stringResource(id = R.string.screen_app_lock_setup_pin_context, appName),
            iconStyle = BigIcon.Style.Default(CompoundIcons.LockSolid()),
        )
    }
}

@Composable
private fun SetupPinContent(
    state: SetupPinState,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    PinEntryTextField(
        pinEntry = state.activePinEntry,
        isSecured = true,
        onValueChange = { entry ->
            state.eventSink(SetupPinEvent.OnPinEntryChanged(entry, state.isConfirmationStep))
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(top = 36.dp)
            .fillMaxWidth()
    )
    if (state.setupPinFailure != null) {
        ErrorDialog(
            title = state.setupPinFailure.title(),
            content = state.setupPinFailure.content(),
            onSubmit = {
                state.eventSink(SetupPinEvent.ClearFailure)
            }
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SetupPinFailure.content(): String {
    return when (this) {
        SetupPinFailure.ForbiddenPin -> stringResource(id = R.string.screen_app_lock_setup_pin_forbidden_dialog_content)
        SetupPinFailure.PinsDoNotMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_content)
    }
}

@Composable
@ReadOnlyComposable
private fun SetupPinFailure.title(): String {
    return when (this) {
        SetupPinFailure.ForbiddenPin -> stringResource(id = R.string.screen_app_lock_setup_pin_forbidden_dialog_title)
        SetupPinFailure.PinsDoNotMatch -> stringResource(id = R.string.screen_app_lock_setup_pin_mismatch_dialog_title)
    }
}

@Composable
@PreviewsDayNight
internal fun SetupPinViewPreview(@PreviewParameter(SetupPinStatePreviewParam::class) state: SetupPinState) {
    ElementPreview {
        SetupPinView(
            state = state,
            onBackClick = {},
        )
    }
}
