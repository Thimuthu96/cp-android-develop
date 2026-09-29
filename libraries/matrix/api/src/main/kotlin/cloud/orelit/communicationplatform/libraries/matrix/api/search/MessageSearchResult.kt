/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.api.search

import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileDetails

/**
 * A single message matching a search query, with its content and sender resolved.
 */
data class MessageSearchResult(
    val roomId: RoomId,
    val eventId: EventId,
    val senderId: UserId,
    val senderProfile: ProfileDetails,
    val content: EventContent,
    /** Origin server timestamp, in milliseconds since the epoch. */
    val timestamp: Long,
)
