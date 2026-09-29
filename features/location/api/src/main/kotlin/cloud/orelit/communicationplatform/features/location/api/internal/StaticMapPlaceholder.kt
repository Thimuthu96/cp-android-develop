/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.api.internal

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.location.api.R
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
internal fun StaticMapPlaceholder(
    painter: Painter,
    canReload: Boolean,
    contentDescription: String?,
    width: Dp,
    height: Dp,
    onLoadMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = width, height = height)
            .clickable(enabled = canReload, onClick = onLoadMapClick)
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(width = width, height = height)
        )
        if (canReload) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = CompoundIcons.Restart(),
                    contentDescription = null
                )
                Text(text = stringResource(id = CommonStrings.action_static_map_load))
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun StaticMapPlaceholderPreview() = ElementPreview {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(false, true)
        .forEach { canReload ->
            StaticMapPlaceholder(
                painter = painterResource(R.drawable.blurred_map),
                canReload = canReload,
                contentDescription = null,
                width = 400.dp,
                height = 200.dp,
                onLoadMapClick = {},
            )
        }
    }
}
