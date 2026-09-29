/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.preferences

import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.components.preferenceIcon
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Slider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun PreferenceSlide(
    title: String,
    @FloatRange(0.0, 1.0)
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
    enabled: Boolean = true,
    summary: String? = null,
    steps: Int = 0,
) {
    ListItem(
        modifier = modifier,
        enabled = enabled,
        leadingContent = preferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            showIconAreaIfNoIcon = showIconAreaIfNoIcon,
        ),
        content = {
            Column {
                Text(
                    style = ElementTheme.typography.fontBodyLgRegular,
                    text = title,
                )
                summary?.let {
                    Text(
                        style = ElementTheme.typography.fontBodyMdRegular,
                        text = summary,
                    )
                }
                Slider(
                    value = value,
                    steps = steps,
                    onValueChange = onValueChange,
                    enabled = enabled,
                )
            }
        }
    )
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceSlidePreview() = ElementThemedPreview {
    Column {
        PreferenceSlide(
            icon = CompoundIcons.UserProfile(),
            title = "Slide",
            summary = "Summary",
            enabled = true,
            value = 0.75F,
            onValueChange = {},
        )
        PreferenceSlide(
            icon = CompoundIcons.UserProfile(),
            title = "Slide",
            summary = "Summary",
            enabled = false,
            value = 0.75F,
            onValueChange = {},
        )
    }
}
