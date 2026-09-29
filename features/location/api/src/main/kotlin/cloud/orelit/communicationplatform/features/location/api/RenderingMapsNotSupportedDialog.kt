/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun RenderingMapsNotSupportedDialog(onSubmit: () -> Unit) {
    ErrorDialog(
        title = stringResource(CommonStrings.vulkan_not_supported_dialog_title_android),
        content = stringResource(CommonStrings.vulkan_not_supported_dialog_content_android),
        onSubmit = onSubmit,
    )
}

@PreviewsDayNight
@Composable
internal fun RenderingMapsNotSupportedDialogPreview() = ElementPreview {
    RenderingMapsNotSupportedDialog(onSubmit = {})
}
