/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.preview

import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.compound.theme.Theme

@Composable
fun ElementPreviewBlack(
    showBackground: Boolean = true,
    content: @Composable () -> Unit
) {
    ElementPreview(
        theme = Theme.Black,
        showBackground = showBackground,
        content = content
    )
}
