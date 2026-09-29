/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.pinned.banner

import androidx.compose.ui.text.AnnotatedString
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.pinned.isDisplayableAsPinnedEvent
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import kotlinx.coroutines.withContext

@Inject
class PinnedMessagesBannerItemFactory(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val formatter: PinnedMessagesBannerFormatter,
) {
    suspend fun create(timelineItem: MatrixTimelineItem): PinnedMessagesBannerItem? = withContext(coroutineDispatchers.computation) {
        when (timelineItem) {
            is MatrixTimelineItem.Event -> {
                val eventId = timelineItem.eventId ?: return@withContext null
                if (!timelineItem.event.content.isDisplayableAsPinnedEvent()) return@withContext null
                val formatted = formatter.format(timelineItem.event)
                PinnedMessagesBannerItem(
                    eventId = eventId,
                    formatted = if (formatted is AnnotatedString) {
                        formatted
                    } else {
                        AnnotatedString(formatted.toString())
                    },
                )
            }
            else -> null
        }
    }
}
