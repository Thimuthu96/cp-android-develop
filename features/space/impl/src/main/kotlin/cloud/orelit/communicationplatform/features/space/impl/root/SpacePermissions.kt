/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.root

import cloud.orelit.communicationplatform.features.space.impl.settings.SpaceSettingsPermissions
import cloud.orelit.communicationplatform.features.space.impl.settings.spaceSettingsPermissions
import cloud.orelit.communicationplatform.libraries.matrix.api.room.StateEventType
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.RoomPermissions

/**
 * Permissions needed for different actions in the Space screen.
 * @param settingsPermissions Permissions related to space settings.
 * @param canEditSpaceGraph Whether the user can edit the space graph (add/remove children).
 */
data class SpacePermissions(
    val settingsPermissions: SpaceSettingsPermissions,
    val canEditSpaceGraph: Boolean,
) {
    companion object {
        val DEFAULT = SpacePermissions(
            settingsPermissions = SpaceSettingsPermissions.DEFAULT,
            canEditSpaceGraph = false,
        )
    }
}

fun RoomPermissions.spacePermissions(): SpacePermissions {
    return SpacePermissions(
        settingsPermissions = spaceSettingsPermissions(),
        canEditSpaceGraph = canOwnUserSendState(StateEventType.SpaceChild) || canOwnUserSendState(StateEventType.SpaceParent),
    )
}
