/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl

import cloud.orelit.communicationplatform.libraries.matrix.api.room.MessageEventType
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.RoomPermissions

/**
 * Represents the permissions a user has in a room.
 * It's dependent of the user's power level in the room.
 */
data class UserEventPermissions(
    val canRedactOwn: Boolean,
    val canRedactOther: Boolean,
    val canSendMessage: Boolean,
    val canSendReaction: Boolean,
    val canPinUnpin: Boolean,
) {
    companion object {
        val DEFAULT = UserEventPermissions(
            canRedactOwn = true,
            canRedactOther = false,
            canSendMessage = true,
            canSendReaction = true,
            canPinUnpin = false
        )
    }
}

fun RoomPermissions.userEventPermissions(): UserEventPermissions {
    return UserEventPermissions(
        canRedactOwn = canOwnUserRedactOwn(),
        canRedactOther = canOwnUserRedactOther(),
        canSendMessage = canOwnUserSendMessage(MessageEventType.RoomMessage),
        canSendReaction = canOwnUserSendMessage(MessageEventType.Reaction),
        canPinUnpin = canOwnUserPinUnpin()
    )
}
