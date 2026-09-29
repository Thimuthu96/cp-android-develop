/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.poll.test.pollcontent

import cloud.orelit.communicationplatform.features.poll.api.pollcontent.PollAnswerItem
import cloud.orelit.communicationplatform.features.poll.api.pollcontent.PollContentState
import cloud.orelit.communicationplatform.features.poll.api.pollcontent.PollContentStateFactory
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import kotlinx.collections.immutable.toImmutableList

class FakePollContentStateFactory : PollContentStateFactory {
    override suspend fun create(eventId: EventId?, isEditable: Boolean, isOwn: Boolean, content: PollContent): PollContentState {
        return PollContentState(
            eventId = eventId,
            question = content.question,
            answerItems = emptyList<PollAnswerItem>().toImmutableList(),
            pollKind = content.kind,
            isPollEditable = isEditable,
            isPollEnded = content.endTime != null,
            isMine = isOwn,
        )
    }
}
