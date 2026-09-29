/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.protection

import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemFileContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemImageContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRoomMembershipContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStateEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemTextContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent

/**
 * Return true if the event must be hidden by default when the setting to hide images and videos is enabled.
 */
fun TimelineItem.mustBeProtected(): Boolean {
    return when (this) {
        is TimelineItem.Event -> {
            if (isMine) {
                false
            } else {
                when (content) {
                    is TimelineItemImageContent,
                    is TimelineItemGalleryContent,
                    is TimelineItemAttachmentsContent,
                    is TimelineItemVideoContent,
                    is TimelineItemStickerContent -> true
                    is TimelineItemAudioContent,
                    is TimelineItemRtcNotificationContent,
                    is TimelineItemEncryptedContent,
                    is TimelineItemFileContent,
                    TimelineItemLegacyCallInviteContent,
                    is TimelineItemLocationContent,
                    is TimelineItemPollContent,
                    TimelineItemRedactedContent,
                    is TimelineItemProfileChangeContent,
                    is TimelineItemRoomMembershipContent,
                    is TimelineItemStateEventContent,
                    is TimelineItemEmoteContent,
                    is TimelineItemNoticeContent,
                    is TimelineItemTextContent,
                    TimelineItemUnknownContent,
                    is TimelineItemVoiceContent -> false
                }
            }
        }
        is TimelineItem.Virtual -> false
        is TimelineItem.GroupedEvents -> false
    }
}
