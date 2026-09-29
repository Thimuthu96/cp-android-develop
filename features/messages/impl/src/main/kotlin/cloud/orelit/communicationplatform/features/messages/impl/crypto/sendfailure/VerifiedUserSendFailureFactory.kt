/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.matrix.api.room.BaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocalEventSendState

@Inject
class VerifiedUserSendFailureFactory(
    private val room: BaseRoom,
) {
    suspend fun create(
        sendState: LocalEventSendState?,
    ): VerifiedUserSendFailure {
        return when (sendState) {
            is LocalEventSendState.Failed.VerifiedUserHasUnsignedDevice -> {
                val userId = sendState.devices.keys.firstOrNull()
                if (userId == null) {
                    VerifiedUserSendFailure.None
                } else {
                    if (userId == room.sessionId) {
                        VerifiedUserSendFailure.UnsignedDevice.FromYou
                    } else {
                        val displayName = room.userDisplayName(userId).getOrNull() ?: userId.value
                        VerifiedUserSendFailure.UnsignedDevice.FromOther(displayName)
                    }
                }
            }
            is LocalEventSendState.Failed.VerifiedUserChangedIdentity -> {
                val userId = sendState.users.firstOrNull()
                if (userId == null) {
                    VerifiedUserSendFailure.None
                } else {
                    val displayName = room.userDisplayName(userId).getOrNull() ?: userId.value
                    VerifiedUserSendFailure.ChangedIdentity(displayName)
                }
            }
            else -> VerifiedUserSendFailure.None
        }
    }
}
