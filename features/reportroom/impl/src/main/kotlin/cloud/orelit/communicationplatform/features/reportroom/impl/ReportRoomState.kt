/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.reportroom.impl

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction

data class ReportRoomState(
    val reason: String,
    val leaveRoom: Boolean,
    val reportAction: AsyncAction<Unit>,
    val eventSink: (ReportRoomEvent) -> Unit
) {
    val canReport: Boolean = reason.isNotBlank()
}
