/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.room

import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.LatestEventValue
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileDetails
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.aMessageContent
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.aProfileDetails

fun aRemoteLatestEvent(
    content: EventContent = aMessageContent(),
    timestamp: Long = 0L,
    isOwn: Boolean = false,
    senderId: UserId = A_USER_ID,
    senderProfile: ProfileDetails = aProfileDetails(),
): LatestEventValue.Remote {
    return LatestEventValue.Remote(
        timestamp = timestamp,
        content = content,
        senderId = senderId,
        senderProfile = senderProfile,
        isOwn = isOwn,
    )
}

fun aLocalLatestEvent(
    content: EventContent = aMessageContent(),
    timestamp: Long = 0L,
    isSending: Boolean = false,
    senderId: UserId = A_USER_ID,
    senderProfile: ProfileDetails = aProfileDetails(),
): LatestEventValue.Local {
    return LatestEventValue.Local(
        timestamp = timestamp,
        content = content,
        senderId = senderId,
        senderProfile = senderProfile,
        isSending = isSending,
    )
}
