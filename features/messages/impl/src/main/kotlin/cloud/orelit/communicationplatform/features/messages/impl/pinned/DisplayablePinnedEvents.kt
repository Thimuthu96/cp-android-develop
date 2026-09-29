/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.pinned

import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

internal fun List<MatrixTimelineItem>.keepDisplayablePinnedEvents(): List<MatrixTimelineItem> {
    val items = filter { it.isDisplayableAsPinnedEvent() }
    return items.filterIndexed { index, item ->
        !item.isDayDivider() || items.getOrNull(index + 1) is MatrixTimelineItem.Event
    }
}

internal fun EventContent.isDisplayableAsPinnedEvent(): Boolean {
    return when (this) {
        UnknownContent,
        is FailedToParseMessageLikeContent,
        is FailedToParseStateContent -> false
        else -> true
    }
}

private fun MatrixTimelineItem.isDisplayableAsPinnedEvent(): Boolean {
    return this !is MatrixTimelineItem.Event || event.content.isDisplayableAsPinnedEvent()
}

private fun MatrixTimelineItem.isDayDivider(): Boolean {
    return this is MatrixTimelineItem.Virtual && virtual is VirtualTimelineItem.DayDivider
}
