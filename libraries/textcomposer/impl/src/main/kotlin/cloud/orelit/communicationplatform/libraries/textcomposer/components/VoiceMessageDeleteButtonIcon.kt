/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.textcomposer.components

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
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun VoiceMessageDeleteButtonIcon(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier.size(24.dp),
        imageVector = CompoundIcons.Delete(),
        contentDescription = stringResource(CommonStrings.a11y_delete),
        tint = if (enabled) {
            ElementTheme.colors.iconCriticalPrimary
        } else {
            ElementTheme.colors.iconDisabled
        },
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageDeleteButtonIconPreview() = ElementPreview {
    Row {
        IconButton(onClick = {}) {
            VoiceMessageDeleteButtonIcon(
                enabled = true,
            )
        }
        IconButton(onClick = {}) {
            VoiceMessageDeleteButtonIcon(
                enabled = false,
            )
        }
    }
}
