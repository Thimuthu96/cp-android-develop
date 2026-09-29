/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.spaces

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceServiceFilter
import org.matrix.rustcomponents.sdk.SpaceFilter as RustSpaceFilter

class SpaceServiceFilterMapper(
    private val spaceRoomMapper: SpaceRoomMapper,
) {
    fun map(spaceFilter: RustSpaceFilter): SpaceServiceFilter {
        return SpaceServiceFilter(
            spaceRoom = spaceRoomMapper.map(spaceFilter.spaceRoom),
            level = spaceFilter.level.toInt(),
            descendants = spaceFilter.descendants.map { RoomId(it) },
        )
    }
}
