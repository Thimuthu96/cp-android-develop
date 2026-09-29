/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.leave

import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom

data class SelectableSpaceRoom(
    val spaceRoom: SpaceRoom,
    val isLastOwner: Boolean,
    val joinedMembersCount: Int,
    val isSelected: Boolean,
)
