/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun RoomPreviewDescriptionAtom(
    description: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = description,
        style = ElementTheme.typography.fontBodyMdRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textPrimary,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
