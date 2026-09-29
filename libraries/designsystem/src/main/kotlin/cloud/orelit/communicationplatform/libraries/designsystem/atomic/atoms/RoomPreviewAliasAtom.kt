/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.text.toDp
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@Composable
fun RoomPreviewAliasAtom(
    alias: String,
    modifier: Modifier = Modifier,
    copiable: Boolean = true
) {
    val clipboard: Clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = modifier
            .clickable(enabled = copiable) {
                coroutineScope.launch {
                    val clipData = ClipData.newPlainText(alias, alias)
                    clipboard.setClipEntry(clipData.toClipEntry())
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = alias,
            style = ElementTheme.typography.fontBodyLgRegular,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ElementTheme.colors.textSecondary,
        )
        if (copiable) {
            Icon(
                imageVector = CompoundIcons.Copy(),
                contentDescription = stringResource(id = CommonStrings.action_copy),
                tint = ElementTheme.colors.iconSecondaryAlpha,
                modifier = Modifier.size(ElementTheme.typography.fontBodyLgRegular.fontSize.toDp())
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomPreviewAliasAtomPreview() = ElementPreview {
    RoomPreviewAliasAtom(
        alias = "#room-alias:matrix.org",
        copiable = true
    )
}
