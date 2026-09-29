/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.notificationsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.features.roomdetails.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode

@Composable
fun RoomNotificationSettingsOption(
    roomNotificationSettingsItem: RoomNotificationSettingsItem,
    onSelectOption: (RoomNotificationSettingsItem) -> Unit,
    displayMentionsOnlyDisclaimer: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
) {
    val mode = roomNotificationSettingsItem.mode
    val title = roomNotificationSettingsItem.title
    val subtitle = when {
        mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY && displayMentionsOnlyDisclaimer -> {
            stringResource(id = R.string.screen_notification_settings_mentions_only_disclaimer)
        }
        else -> null
    }
    ListItem(
        modifier = modifier,
        enabled = enabled,
        content = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = ListItemContent.RadioButton(selected = isSelected),
        onClick = { onSelectOption(roomNotificationSettingsItem) },
    )
}

@PreviewsDayNight
@Composable
internal fun RoomNotificationSettingsOptionPreview() = ElementPreview {
    Column {
        for ((index, item) in roomNotificationSettingsItems().withIndex()) {
            RoomNotificationSettingsOption(
                roomNotificationSettingsItem = item,
                onSelectOption = {},
                isSelected = index == 0,
                enabled = index != 2,
                displayMentionsOnlyDisclaimer = index == 1,
            )
        }
    }
}
