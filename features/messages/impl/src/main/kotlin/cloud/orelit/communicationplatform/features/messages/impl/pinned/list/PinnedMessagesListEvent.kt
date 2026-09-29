/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.pinned.list

import cloud.orelit.communicationplatform.features.messages.impl.actionlist.model.TimelineItemAction
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventOrTransactionId

sealed interface PinnedMessagesListEvent {
    data class HandleAction(val action: TimelineItemAction, val event: TimelineItem.Event) : PinnedMessagesListEvent
    data class OpenThread(val threadRootId: ThreadId) : PinnedMessagesListEvent
    data class ToggleReaction(val emoji: String, val eventOrTransactionId: EventOrTransactionId) : PinnedMessagesListEvent
}
