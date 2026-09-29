/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModelPreviewParam
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
internal fun TimelineItemDaySeparatorView(
    model: TimelineItemDaySeparatorModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier
                .semantics {
                    heading()
                },
            text = model.formattedDate,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemDaySeparatorViewPreview(
    @PreviewParameter(TimelineItemDaySeparatorModelPreviewParam::class) model: TimelineItemDaySeparatorModel
) = ElementPreview {
    TimelineItemDaySeparatorView(
        model = model,
    )
}
