/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.components.preferenceIcon
import cloud.orelit.communicationplatform.libraries.designsystem.icons.CompoundDrawables
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.toEnabledColor
import cloud.orelit.communicationplatform.libraries.designsystem.toSecondaryEnabledColor

@Composable
fun PreferenceCheckbox(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
) {
    ListItem(
        modifier = modifier,
        onClick = onCheckedChange.takeIf { enabled }?.let { { onCheckedChange(!isChecked) } },
        leadingContent = preferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            showIconAreaIfNoIcon = showIconAreaIfNoIcon,
        ),
        content = {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = enabled.toEnabledColor(),
            )
        },
        supportingContent = supportingText?.let {
            {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = it,
                    color = enabled.toSecondaryEnabledColor(),
                )
            }
        },
        trailingContent = ListItemContent.Checkbox(
            checked = isChecked,
            enabled = enabled,
        ),
        enabled = enabled,
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceCheckboxPreview() = ElementThemedPreview {
    Column {
        PreferenceCheckbox(
            title = "Checkbox",
            iconResourceId = CompoundDrawables.ic_compound_threads,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = CompoundDrawables.ic_compound_threads,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = CompoundDrawables.ic_compound_threads,
            enabled = false,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = null,
            showIconAreaIfNoIcon = true,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
        PreferenceCheckbox(
            title = "Checkbox with supporting text",
            supportingText = "Supporting text",
            iconResourceId = null,
            showIconAreaIfNoIcon = false,
            enabled = true,
            isChecked = true,
            onCheckedChange = {},
        )
    }
}
