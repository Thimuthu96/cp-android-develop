/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.messageFromMeBackground

@Composable
fun PlaybackSpeedButton(
    speed: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val speedText = when (speed) {
        0.5f -> "0.5×"
        1.0f -> "1×"
        1.5f -> "1.5×"
        2.0f -> "2×"
        else -> "$speed×"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = ElementTheme.colors.bgCanvasDefault,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = speedText,
            color = ElementTheme.colors.iconSecondary,
            style = ElementTheme.typography.fontBodyXsMedium,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PlaybackSpeedButtonPreview() = ElementPreview {
    Row(
        modifier = Modifier
            .background(ElementTheme.colors.messageFromMeBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(0.5f, 1.0f, 1.5f, 2.0f, 3.0f).forEach { speed ->
            PlaybackSpeedButton(
                speed = speed,
                onClick = {},
            )
        }
    }
}
