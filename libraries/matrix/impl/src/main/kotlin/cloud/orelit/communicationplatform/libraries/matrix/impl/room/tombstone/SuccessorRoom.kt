/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.room.tombstone

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.tombstone.SuccessorRoom
import org.matrix.rustcomponents.sdk.SuccessorRoom as RustSuccessorRoom

fun RustSuccessorRoom.map(): SuccessorRoom {
    return SuccessorRoom(
        roomId = RoomId(roomId),
        reason = reason
    )
}
