/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.group

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Surface
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

private val CORNER_RADIUS = 8.dp

@Composable
fun GroupHeaderView(
    text: String,
    isExpanded: Boolean,
    @Suppress("UNUSED_PARAMETER") isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ignore isHighlighted for now, we need a design decision on it.
    val backgroundColor = Color.Transparent
    val shape = RoundedCornerShape(CORNER_RADIUS)

    val expandedStateDescription = stringResource(CommonStrings.a11y_state_expanded)
    val collapsedStateDescription = stringResource(CommonStrings.a11y_state_collapsed)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isExpanded,
                onValueChange = { onClick() },
                role = Role.DropdownList,
            )
            .clearAndSetSemantics {
                contentDescription = text
                stateDescription = if (isExpanded) expandedStateDescription else collapsedStateDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .clip(shape)
                .clickable(onClick = onClick),
            color = backgroundColor,
            shape = shape,
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
                val rotation: Float by animateFloatAsState(
                    targetValue = if (isExpanded) 90f else 0f,
                    animationSpec = tween(
                        delayMillis = 0,
                        durationMillis = 300,
                    ),
                    label = "chevron"
                )
                Icon(
                    modifier = Modifier.rotate(rotation),
                    imageVector = CompoundIcons.ChevronRight(),
                    contentDescription = null,
                    tint = ElementTheme.colors.iconSecondary
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun GroupHeaderViewPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GroupHeaderView(
            text = "8 room changes (expanded)",
            isExpanded = true,
            isHighlighted = false,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (not expanded)",
            isExpanded = false,
            isHighlighted = false,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (expanded/h)",
            isExpanded = true,
            isHighlighted = true,
            onClick = {}
        )
        GroupHeaderView(
            text = "8 room changes (not expanded/h)",
            isExpanded = false,
            isHighlighted = true,
            onClick = {}
        )
    }
}
