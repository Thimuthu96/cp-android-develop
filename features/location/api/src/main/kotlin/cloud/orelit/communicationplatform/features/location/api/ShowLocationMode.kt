/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.api

import android.os.Parcelable
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.location.AssetType
import kotlinx.parcelize.Parcelize

sealed interface ShowLocationMode : Parcelable {
    @Parcelize
    data class Static(
        val location: Location,
        val senderName: String,
        val senderId: UserId,
        val senderAvatarUrl: String?,
        val timestamp: Long,
        val assetType: AssetType?,
    ) : ShowLocationMode

    @Parcelize
    data class Live(
        val senderId: UserId
    ) : ShowLocationMode
}
