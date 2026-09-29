/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.api

import android.os.Parcelable
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallData(
    val sessionId: SessionId,
    val roomId: RoomId,
    val isAudioCall: Boolean
) : NodeInputs, Parcelable
