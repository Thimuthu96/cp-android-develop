/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.fileviewer

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia

data class FileViewerState(
    val filename: String,
    val localMedia: AsyncData<LocalMedia>,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (FileViewerEvent) -> Unit,
)
