/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.notificationsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.features.roomdetails.impl.R
import cloud.orelit.communicationplatform.libraries.core.bool.orTrue
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncActionView
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItemStyle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar

@Composable
fun UserDefinedRoomNotificationSettingsView(
    state: RoomNotificationSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            UserDefinedRoomNotificationSettingsTopBar(
                roomName = state.roomName,
                onBackClick = { onBackClick() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val roomNotificationSettings = state.roomNotificationSettings.dataOrNull()
            if (roomNotificationSettings != null && state.displayNotificationMode != null) {
                RoomNotificationSettingsOptions(
                    selected = state.displayNotificationMode,
                    enabled = !state.displayIsDefault.orTrue(),
                    displayMentionsOnlyDisclaimer = state.displayMentionsOnlyDisclaimer,
                    onSelectOption = {
                        state.eventSink(RoomNotificationSettingsEvent.ChangeRoomNotificationMode(it.mode))
                    },
                )
            }

            ListItem(
                content = { Text(stringResource(R.string.screen_room_notification_settings_edit_remove_setting)) },
                style = ListItemStyle.Destructive,
                onClick = {
                    state.eventSink(RoomNotificationSettingsEvent.DeleteCustomNotification)
                }
            )

            AsyncActionView(
                async = state.setNotificationSettingAction,
                onSuccess = {},
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvent.ClearSetNotificationError) },
            )

            AsyncActionView(
                async = state.restoreDefaultAction,
                onSuccess = { onBackClick() },
                errorMessage = { stringResource(R.string.screen_notification_settings_edit_failed_updating_default_mode) },
                onErrorDismiss = { state.eventSink(RoomNotificationSettingsEvent.ClearRestoreDefaultError) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserDefinedRoomNotificationSettingsTopBar(
    roomName: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        titleStr = roomName,
        navigationIcon = { BackButton(onClick = onBackClick) },
    )
}

@PreviewsDayNight
@Composable
internal fun UserDefinedRoomNotificationSettingsViewPreview(
    @PreviewParameter(UserDefinedRoomNotificationSettingsStatePreviewParam::class) state: RoomNotificationSettingsState
) = ElementPreview {
    UserDefinedRoomNotificationSettingsView(
        state = state,
        onBackClick = {},
    )
}
