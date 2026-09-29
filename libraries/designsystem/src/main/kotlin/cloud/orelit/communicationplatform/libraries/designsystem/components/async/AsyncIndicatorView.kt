/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.async

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Surface
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
internal fun AsyncIndicatorView(
    text: String,
    spacing: Dp,
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    leadingContent: @Composable (() -> Unit)?,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .padding(elevation)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = elevation,
        ) {
            Row(
                modifier = Modifier
                    .background(color = ElementTheme.colors.bgSubtleSecondary)
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                leadingContent?.let { view ->
                    view()
                }
                Text(
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyMdMedium
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AsyncIndicatorLoadingPreview() {
    ElementPreview {
        AsyncIndicator.Loading(text = "Loading")
    }
}

@PreviewsDayNight
@Composable
internal fun AsyncIndicatorFailurePreview() {
    ElementPreview {
        AsyncIndicator.Failure(text = "Failed")
    }
}
