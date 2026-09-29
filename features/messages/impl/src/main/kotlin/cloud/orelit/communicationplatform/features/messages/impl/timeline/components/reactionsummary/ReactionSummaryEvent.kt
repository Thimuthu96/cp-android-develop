/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.reactionsummary

import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.AggregatedReaction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId

sealed interface ReactionSummaryEvent {
    data object Clear : ReactionSummaryEvent
    data class ShowReactionSummary(val eventId: EventId, val reactions: List<AggregatedReaction>, val selectedKey: String) : ReactionSummaryEvent
}
