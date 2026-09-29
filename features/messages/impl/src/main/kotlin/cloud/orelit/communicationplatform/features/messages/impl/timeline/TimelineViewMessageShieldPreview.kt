/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.aCriticalShield
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.aTimelineProtectionState
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.toImmutableList

@PreviewsDayNight
@Composable
internal fun TimelineViewMessageShieldPreview() = ElementPreview {
    val timelineItems = aTimelineItemList(aTimelineItemTextContent())
    // For consistency, ensure that there is a message in the timeline (the last one) with an error.
    val messageShield = aCriticalShield()
    val items = listOf(
        (timelineItems.first() as TimelineItem.Event).copy(
            messageShieldProvider = { messageShield },
        )
    ) + timelineItems.drop(1)
    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(
                timelineItems = items.toImmutableList(),
                messageShield = messageShield,
            ),
            timelineProtectionState = aTimelineProtectionState(),
            onUserDataClick = {},
            onLinkClick = {},
            onContentClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            onJoinCallClick = {},
            onGalleryItemClick = { _, _ -> },
            forceJumpToBottomVisibility = true,
        )
    }
}
