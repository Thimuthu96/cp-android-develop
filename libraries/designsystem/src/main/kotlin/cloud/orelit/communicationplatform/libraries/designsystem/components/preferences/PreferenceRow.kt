/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

/**
 * Simple Row with which follow design for preferences.
 */
@Composable
fun PreferenceRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    ListItem(
        modifier = modifier,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        }
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceRowPreview() = ElementThemedPreview {
    PreferenceRow {
        Text(text = "Content")
    }
}
