/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.location.api.Location
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.RtcNotificationState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatterMode
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.toMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LiveLocationContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.PollContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RedactedContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

@Inject
class TimelineItemContentFactory(
    private val messageFactory: TimelineItemContentMessageFactory,
    private val redactedMessageFactory: TimelineItemContentRedactedFactory,
    private val stickerFactory: TimelineItemContentStickerFactory,
    private val pollFactory: TimelineItemContentPollFactory,
    private val utdFactory: TimelineItemContentUTDFactory,
    private val roomMembershipFactory: TimelineItemContentRoomMembershipFactory,
    private val profileChangeFactory: TimelineItemContentProfileChangeFactory,
    private val stateFactory: TimelineItemContentStateFactory,
    private val failedToParseMessageFactory: TimelineItemContentFailedToParseMessageFactory,
    private val failedToParseStateFactory: TimelineItemContentFailedToParseStateFactory,
    private val sessionId: SessionId,
    private val dateFormatter: DateFormatter,
    private val stringProvider: StringProvider,
) {
    suspend fun create(eventTimelineItem: EventTimelineItem, roomMembers: List<RoomMember>): TimelineItemEventContent {
        return create(
            itemContent = eventTimelineItem.content,
            eventId = eventTimelineItem.eventId,
            isEditable = eventTimelineItem.isEditable,
            sender = eventTimelineItem.sender,
            senderProfile = eventTimelineItem.senderProfile,
            roomMembers = roomMembers,
        )
    }

    suspend fun create(
        itemContent: EventContent,
        eventId: EventId?,
        isEditable: Boolean,
        sender: UserId,
        senderProfile: ProfileDetails,
        roomMembers: List<RoomMember> = emptyList(),
    ): TimelineItemEventContent {
        val isOutgoing = sessionId == sender
        return when (itemContent) {
            is FailedToParseMessageLikeContent -> failedToParseMessageFactory.create(itemContent)
            is FailedToParseStateContent -> failedToParseStateFactory.create(itemContent)
            is MessageContent -> {
                messageFactory.create(
                    senderId = sender,
                    senderProfile = senderProfile,
                    content = itemContent,
                    eventId = eventId,
                )
            }
            is ProfileChangeContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                profileChangeFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is RedactedContent -> redactedMessageFactory.create(itemContent)
            is RoomMembershipContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                roomMembershipFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is LegacyCallInviteContent -> TimelineItemLegacyCallInviteContent
            is StateContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                stateFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is StickerContent -> stickerFactory.create(itemContent)
            is PollContent -> pollFactory.create(eventId, isEditable, isOutgoing, itemContent)
            is UnableToDecryptContent -> utdFactory.create(itemContent)
            is CallNotifyContent -> TimelineItemRtcNotificationContent(
                callIntent = itemContent.callIntent,
                state = when {
                    itemContent.activeMembers.isNotEmpty() -> {
                        RtcNotificationState.Active(
                            joinedMembers = itemContent.activeMembers.map { active ->
                                roomMembers.find {
                                    it.userId == active
                                }?.toMatrixUser() ?: MatrixUser(active)
                            },
                            isJoined = itemContent.isJoined,
                            callStartTsMillis = itemContent.callStartTsMillis,
                            callIntent = itemContent.callIntent
                        )
                    }
                    else -> {
                        if (itemContent.declinedBy.isEmpty()) {
                            RtcNotificationState.Started
                        } else {
                            RtcNotificationState.Declined(itemContent.declinedBy.any { it == sessionId })
                        }
                    }
                }
            )
            is UnknownContent -> TimelineItemUnknownContent
            is LiveLocationContent -> {
                val lastKnownLocation = itemContent.locations.mapNotNull { beacon ->
                    Location.fromGeoUri(beacon.geoUri)
                }.lastOrNull()

                val endsAt = dateFormatter.format(
                    timestamp = itemContent.endTimestamp,
                    mode = DateFormatterMode.TimeOnly
                )
                // Always create content, location can be null for "loading/waiting" state
                TimelineItemLocationContent(
                    description = itemContent.description?.trimEnd(),
                    assetType = itemContent.assetType,
                    senderId = sender,
                    senderProfile = senderProfile,
                    mode = TimelineItemLocationContent.Mode.Live(
                        lastKnownLocation = lastKnownLocation,
                        isActive = itemContent.isLive,
                        endsAt = stringProvider.getString(CommonStrings.common_ends_at, endsAt),
                        endTimestamp = itemContent.endTimestamp,
                        isOwnUser = sessionId == sender
                    ),
                )
            }
        }
    }
}
