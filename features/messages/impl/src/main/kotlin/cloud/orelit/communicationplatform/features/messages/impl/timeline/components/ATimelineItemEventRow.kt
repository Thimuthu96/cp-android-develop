/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineRoomInfo
import cloud.orelit.communicationplatform.features.messages.impl.timeline.aTimelineRoomInfo
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.TimelineProtectionState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.aTimelineProtectionState
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline

// For previews
@Composable
internal fun ATimelineItemEventRow(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode = Timeline.Mode.Live,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    isLastOutgoingMessage: Boolean = false,
    timelineProtectionState: TimelineProtectionState = aTimelineProtectionState(),
    displayThreadSummaries: Boolean = false,
) = TimelineItemEventRow(
    event = event,
    timelineMode = timelineMode,
    timelineRoomInfo = timelineRoomInfo,
    timelineProtectionState = timelineProtectionState,
    isLastOutgoingMessage = isLastOutgoingMessage,
    displayThreadSummaries = displayThreadSummaries,
    onEventClick = {},
    onGalleryItemClick = {},
    onLongClick = {},
    onLinkClick = {},
    onLinkLongClick = {},
    onUserDataClick = {},
    inReplyToClick = {},
    onReactionClick = { _, _ -> },
    onReactionLongClick = { _, _ -> },
    onMoreReactionsClick = {},
    onReadReceiptClick = {},
    onSwipeToReply = {},
    eventSink = {},
)
