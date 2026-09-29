/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.groups

import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemFileContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemImageContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRoomMembershipContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStateEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LiveLocationContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RedactedContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent

/**
 * Return true when every event in the group is a redacted (deleted) message, i.e. the group is a
 * collapsed run of deleted messages rather than the usual run of room state changes. Used to pick
 * the group header label. An empty group is not considered a redacted group.
 */
internal fun TimelineItem.GroupedEvents.isRedactedMessagesGroup(): Boolean =
    events.isNotEmpty() && events.all { it.content is TimelineItemRedactedContent }

/**
 * Return true if the Event can be grouped in a collapse/expand block
 * When [canBeGrouped] returns a value, [canBeDisplayedInBubbleBlock] MUST return the opposite value.
 * Since the receiving type are not the same, the two functions exist.
 */
internal fun TimelineItem.Event.canBeGrouped(): Boolean {
    return when (content) {
        is TimelineItemTextBasedContent,
        is TimelineItemEncryptedContent,
        is TimelineItemImageContent,
        is TimelineItemGalleryContent,
        is TimelineItemAttachmentsContent,
        is TimelineItemStickerContent,
        is TimelineItemFileContent,
        is TimelineItemVideoContent,
        is TimelineItemAudioContent,
        is TimelineItemLocationContent,
        is TimelineItemPollContent,
        is TimelineItemVoiceContent,
        TimelineItemRedactedContent,
        TimelineItemUnknownContent,
        is TimelineItemLegacyCallInviteContent,
        is TimelineItemRtcNotificationContent -> false
        is TimelineItemProfileChangeContent,
        is TimelineItemRoomMembershipContent,
        is TimelineItemStateEventContent -> true
    }
}

/**
 * Return true if the Event can be grouped in a block of message bubbles.
 * When [canBeDisplayedInBubbleBlock] returns a value, [canBeGrouped] MUST return the opposite value.
 * Since the receiving type are not the same, the two functions exist.
 */
internal fun MatrixTimelineItem.Event.canBeDisplayedInBubbleBlock(): Boolean {
    return when (event.content) {
        // Can be grouped
        is FailedToParseMessageLikeContent,
        is MessageContent,
        is RedactedContent,
        is StickerContent,
        is PollContent,
        is UnableToDecryptContent,
        is LiveLocationContent -> true
        // Can't be grouped
        is FailedToParseStateContent,
        is ProfileChangeContent,
        is RoomMembershipContent,
        UnknownContent,
        is LegacyCallInviteContent,
        is CallNotifyContent,
        is StateContent -> false
    }
}
