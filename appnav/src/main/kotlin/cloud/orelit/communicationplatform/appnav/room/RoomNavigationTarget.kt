/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.room

import android.os.Parcelable
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed interface RoomNavigationTarget : Parcelable {
    @Parcelize
    data class Root(
        val eventId: EventId? = null,
        @IgnoredOnParcel val joinedRoom: JoinedRoom? = null,
    ) : RoomNavigationTarget

    @Parcelize
    data object Details : RoomNavigationTarget

    @Parcelize
    data object NotificationSettings : RoomNavigationTarget
}
