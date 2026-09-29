/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cloud.orelit.communicationplatform.compound.theme.ElementTheme

val bgCanvasWithTransparency: Color
    @Composable
    get() = ElementTheme.colors.bgCanvasDefault.copy(alpha = 0.6f)
