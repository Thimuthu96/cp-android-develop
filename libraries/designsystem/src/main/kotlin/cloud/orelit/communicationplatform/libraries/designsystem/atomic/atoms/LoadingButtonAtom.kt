/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun LoadingButtonAtom(
    modifier: Modifier = Modifier,
) = Button(
    modifier = modifier.fillMaxWidth(),
    enabled = false,
    showProgress = true,
    text = stringResource(CommonStrings.common_loading),
    onClick = {},
)
