/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.settings

import cloud.orelit.communicationplatform.features.roomdetailsedit.api.RoomDetailsEditPermissions
import cloud.orelit.communicationplatform.features.roomdetailsedit.api.roomDetailsEditPermissions
import cloud.orelit.communicationplatform.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import cloud.orelit.communicationplatform.features.securityandprivacy.api.securityAndPrivacyPermissions
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.RoomPermissions
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.canEditRolesAndPermissions

data class SpaceSettingsPermissions(
    val editDetailsPermissions: RoomDetailsEditPermissions,
    val canEditRolesAndPermissions: Boolean,
    val securityAndPrivacyPermissions: SecurityAndPrivacyPermissions,
) {
    fun hasAny(joinRule: JoinRule?): Boolean {
        return editDetailsPermissions.hasAny ||
            canEditRolesAndPermissions ||
            securityAndPrivacyPermissions.hasAny(isSpace = true, joinRule = joinRule)
    }

    companion object {
        val DEFAULT = SpaceSettingsPermissions(
            editDetailsPermissions = RoomDetailsEditPermissions.DEFAULT,
            canEditRolesAndPermissions = false,
            securityAndPrivacyPermissions = SecurityAndPrivacyPermissions.DEFAULT,
        )
    }
}

fun RoomPermissions.spaceSettingsPermissions(): SpaceSettingsPermissions {
    return SpaceSettingsPermissions(
        editDetailsPermissions = roomDetailsEditPermissions(),
        canEditRolesAndPermissions = canEditRolesAndPermissions(),
        securityAndPrivacyPermissions = securityAndPrivacyPermissions(),
    )
}
