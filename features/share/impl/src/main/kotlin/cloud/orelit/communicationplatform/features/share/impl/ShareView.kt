/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.share.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncActionView
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

@Composable
fun ShareView(
    state: ShareState,
    onShareSuccess: (List<RoomId>) -> Unit,
) {
    AsyncActionView(
        async = state.shareAction,
        onSuccess = {
            onShareSuccess(it)
        },
        onErrorDismiss = {
            state.eventSink(ShareEvent.ClearError)
        },
    )
}

@PreviewsDayNight
@Composable
internal fun ShareViewPreview(@PreviewParameter(ShareStatePreviewParam::class) state: ShareState) = ElementPreview {
    ShareView(
        state = state,
        onShareSuccess = {}
    )
}
