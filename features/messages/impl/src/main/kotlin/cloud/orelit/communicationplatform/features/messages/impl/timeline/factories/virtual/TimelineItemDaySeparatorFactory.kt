/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.virtual

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.virtual.TimelineItemVirtualModel
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatterMode
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

@Inject
class TimelineItemDaySeparatorFactory(
    private val dateFormatter: DateFormatter,
) {
    fun create(virtualItem: VirtualTimelineItem.DayDivider): TimelineItemVirtualModel {
        val formattedDate = dateFormatter.format(
            timestamp = virtualItem.timestamp,
            mode = DateFormatterMode.Day,
            useRelative = true,
        )
        return TimelineItemDaySeparatorModel(
            formattedDate = formattedDate
        )
    }
}
