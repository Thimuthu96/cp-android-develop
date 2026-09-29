/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

/**
 * A view to display when the media content is invalid or dangerous.
 */
@Composable
fun InvalidContentView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    ContentErrorView(
        title = stringResource(CommonStrings.content_scanner_unsafe_title),
        message = stringResource(CommonStrings.content_scanner_unsafe_message),
        modifier = modifier,
        contentPadding = contentPadding,
        onTextLayout = onTextLayout,
    )
}

@PreviewsDayNight
@Composable
internal fun InvalidContentViewPreview() = ElementPreview {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InvalidContentView()
    }
}
