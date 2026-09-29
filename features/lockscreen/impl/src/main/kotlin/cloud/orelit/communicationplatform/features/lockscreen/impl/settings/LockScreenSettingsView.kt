/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.lockscreen.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferenceCategory
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferenceDivider
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferencePage
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferenceSwitch
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItemStyle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun LockScreenSettingsView(
    state: LockScreenSettingsState,
    onChangePinClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        title = stringResource(id = cloud.orelit.communicationplatform.libraries.ui.strings.R.string.common_screen_lock),
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        PreferenceCategory(showTopDivider = false) {
            ListItem(
                content = {
                    Text(stringResource(id = R.string.screen_app_lock_settings_change_pin))
                },
                onClick = onChangePinClick,
            )
            PreferenceDivider()
            if (state.showRemovePinOption) {
                ListItem(
                    content = {
                        Text(stringResource(id = R.string.screen_app_lock_settings_remove_pin))
                    },
                    style = ListItemStyle.Destructive,
                    onClick = {
                        state.eventSink(LockScreenSettingsEvent.OnRemovePin)
                    }
                )
            }
            if (state.showToggleBiometric) {
                PreferenceDivider()
                PreferenceSwitch(
                    title = stringResource(id = R.string.screen_app_lock_settings_enable_biometric_unlock),
                    isChecked = state.isBiometricEnabled,
                    onCheckedChange = {
                        state.eventSink(LockScreenSettingsEvent.ToggleBiometricAllowed)
                    }
                )
            }
        }
    }
    if (state.showRemovePinConfirmation) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_app_lock_settings_remove_pin_alert_title),
            content = stringResource(id = R.string.screen_app_lock_settings_remove_pin_alert_message),
            onSubmitClick = {
                state.eventSink(LockScreenSettingsEvent.ConfirmRemovePin)
            },
            onDismiss = {
                state.eventSink(LockScreenSettingsEvent.CancelRemovePin)
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun LockScreenSettingsViewPreview(
    @PreviewParameter(LockScreenSettingsStatePreviewParam::class) state: LockScreenSettingsState,
) {
    ElementPreview {
        LockScreenSettingsView(
            state = state,
            onChangePinClick = {},
            onBackClick = {},
        )
    }
}
