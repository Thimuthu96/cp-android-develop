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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun RoomPreviewTitleAtom(
    title: String,
    modifier: Modifier = Modifier,
    fontStyle: FontStyle? = null,
    style: TextStyle = ElementTheme.typography.fontHeadingLgBold
) {
    Text(
        modifier = modifier,
        text = title,
        style = style,
        textAlign = TextAlign.Center,
        fontStyle = fontStyle,
        color = ElementTheme.colors.textPrimary,
    )
}
