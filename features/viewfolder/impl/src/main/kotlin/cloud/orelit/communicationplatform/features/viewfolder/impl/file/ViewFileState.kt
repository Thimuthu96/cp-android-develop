/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.viewfolder.impl.file

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData

data class ViewFileState(
    val name: String,
    val lines: AsyncData<List<String>>,
    val colorationMode: ColorationMode,
    val eventSink: (ViewFileEvent) -> Unit,
)

enum class ColorationMode {
    Logcat,
    RustLogs,
    None,
}
