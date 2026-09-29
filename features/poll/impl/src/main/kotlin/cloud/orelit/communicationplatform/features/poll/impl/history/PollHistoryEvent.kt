/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.poll.impl.history

import cloud.orelit.communicationplatform.features.poll.impl.history.model.PollHistoryFilter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId

sealed interface PollHistoryEvent {
    data object LoadMore : PollHistoryEvent
    data class SelectPollAnswer(val pollStartId: EventId, val answerId: String) : PollHistoryEvent
    data class EndPoll(val pollStartId: EventId) : PollHistoryEvent
    data class SelectFilter(val filter: PollHistoryFilter) : PollHistoryEvent
}
