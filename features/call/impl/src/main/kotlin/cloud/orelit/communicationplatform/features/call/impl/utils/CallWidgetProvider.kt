/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.utils

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.widget.MatrixWidgetDriver

interface CallWidgetProvider {
    suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        isAudioCall: Boolean,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<GetWidgetResult>

    data class GetWidgetResult(
        val driver: MatrixWidgetDriver,
        val url: String,
    )
}
