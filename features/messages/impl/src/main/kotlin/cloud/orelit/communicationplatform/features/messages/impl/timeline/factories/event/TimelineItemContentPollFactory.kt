/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContent
import cloud.orelit.communicationplatform.features.poll.api.pollcontent.PollContentStateFactory
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent

@Inject
class TimelineItemContentPollFactory(
    private val pollContentStateFactory: PollContentStateFactory,
) {
    suspend fun create(
        eventId: EventId?,
        isEditable: Boolean,
        isOwn: Boolean,
        content: PollContent,
    ): TimelineItemEventContent {
        val pollContentState = pollContentStateFactory.create(eventId, isEditable, isOwn, content)
        return TimelineItemPollContent(
            isMine = pollContentState.isMine,
            isEditable = pollContentState.isPollEditable,
            eventId = eventId,
            question = pollContentState.question,
            answerItems = pollContentState.answerItems,
            pollKind = pollContentState.pollKind,
            isEnded = pollContentState.isPollEnded,
            isEdited = content.isEdited
        )
    }
}
