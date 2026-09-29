/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = ElementNavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = ElementNavigationBarDefaults.tonalElevation,
    windowInsets: WindowInsets = ElementNavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
        content = content
    )
}

object ElementNavigationBarDefaults {
    val containerColor: Color
        @Composable get() = if (ElementTheme.isLightTheme) {
            ElementTheme.colors.bgSubtlePrimary
        } else {
            ElementTheme.colors.textOnSolidPrimary
        }

    val tonalElevation: Dp = NavigationBarDefaults.Elevation

    val windowInsets: WindowInsets
        @Composable get() = NavigationBarDefaults.windowInsets
}

@Preview(group = PreviewGroup.AppBars)
@Composable
internal fun NavigationBarPreview() = ElementThemedPreview {
    NavigationBar {
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 5,
                    isCritical = false,
                )
            },
            label = {
                NavigationBarText(
                    text = "Chats"
                )
            },
            selected = true,
            onClick = {},
        )
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 5,
                    isCritical = true,
                )
            },
            label = {
                NavigationBarText(
                    text = "Teams"
                )
            },
            selected = false,
            onClick = {},
        )
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 0,
                    isCritical = false,
                )
            },
            label = {
                NavigationBarText(
                    text = "Other"
                )
            },
            selected = false,
            onClick = {},
        )
    }
}
