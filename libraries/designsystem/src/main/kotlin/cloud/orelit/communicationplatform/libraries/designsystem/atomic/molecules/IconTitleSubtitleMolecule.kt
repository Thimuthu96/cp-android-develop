/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.BetaLabel
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

/**
 * IconTitleSubtitleMolecule is a molecule which displays an icon, a title and a subtitle.
 *
 * @param title the title to display
 * @param subTitle the subtitle to display
 * @param iconStyle the style of the [BigIcon] to display
 * @param modifier the modifier to apply to this layout
 * @param showBetaLabel whether to show a "BETA" label next to the title
 */
@Composable
fun IconTitleSubtitleMolecule(
    title: String,
    subTitle: String?,
    iconStyle: BigIcon.Style,
    modifier: Modifier = Modifier,
    showBetaLabel: Boolean = false,
) {
    Column(modifier) {
        BigIcon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = iconStyle,
        )
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .semantics {
                        heading()
                    },
                textAlign = TextAlign.Center,
                style = ElementTheme.typography.fontHeadingMdBold,
                color = ElementTheme.colors.textPrimary,
            )
            if (showBetaLabel) {
                BetaLabel()
            }
        }
        if (subTitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun IconTitleSubtitleMoleculePreview() = ElementPreview {
    IconTitleSubtitleMolecule(
        iconStyle = BigIcon.Style.Default(CompoundIcons.Chat()),
        title = "Title",
        subTitle = "Subtitle",
    )
}
