/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.poll.api.pollcontent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun PollTitleView(
    title: String,
    isPollEnded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isPollEnded) {
            Icon(
                imageVector = CompoundIcons.PollsEnd(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll_end),
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = CompoundIcons.Polls(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium
        )
    }
}

@PreviewsDayNight
@Composable
internal fun PollTitleViewPreview() = ElementPreview {
    PollTitleView(
        title = "What is your favorite color?",
        isPollEnded = false
    )
}
