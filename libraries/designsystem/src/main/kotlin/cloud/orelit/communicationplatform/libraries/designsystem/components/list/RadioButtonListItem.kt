/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItemStyle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun RadioButtonListItem(
    headline: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailingContent: ListItemContent? = null,
    style: ListItemStyle = ListItemStyle.Default,
    enabled: Boolean = true,
    compactLayout: Boolean = false,
) {
    ListItem(
        modifier = modifier,
        content = { Text(headline) },
        supportingContent = supportingText?.let { @Composable { Text(it) } },
        leadingContent = ListItemContent.RadioButton(
            selected = selected,
            enabled = enabled,
            compact = compactLayout,
        ),
        trailingContent = trailingContent,
        style = style,
        enabled = enabled,
        onClick = onSelect,
    )
}
