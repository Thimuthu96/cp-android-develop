/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.apperror.api

import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun AppErrorView(
    state: AppErrorState,
) {
    if (state is AppErrorState.Error) {
        AppErrorViewContent(
            title = state.title,
            body = state.body,
            onDismiss = state.dismiss,
        )
    }
}

@Composable
private fun AppErrorViewContent(
    title: String,
    body: String,
    onDismiss: () -> Unit = { },
) {
    ErrorDialog(
        title = title,
        content = body,
        onSubmit = onDismiss,
    )
}

@PreviewsDayNight
@Composable
internal fun AppErrorViewPreview() = ElementPreview {
    AppErrorView(
        state = aAppErrorState()
    )
}
