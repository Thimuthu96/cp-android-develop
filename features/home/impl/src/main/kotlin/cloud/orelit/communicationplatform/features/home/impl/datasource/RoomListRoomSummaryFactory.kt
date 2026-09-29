/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.datasource

import cloud.orelit.communicationplatform.features.home.impl.model.LatestEvent
import cloud.orelit.communicationplatform.features.home.impl.model.RoomListRoomSummary
import cloud.orelit.communicationplatform.features.home.impl.model.RoomSummaryDisplayType
import cloud.orelit.communicationplatform.libraries.core.extensions.orEmpty
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatterMode
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.eventformatter.api.RoomLatestEventFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CallIntentConsensus
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.LatestEventValue
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomSummary
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.dmUserStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.toInviteSender
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableList

@Inject
class RoomListRoomSummaryFactory(
    private val dateFormatter: DateFormatter,
    private val roomLatestEventFormatter: RoomLatestEventFormatter,
) {
    fun create(roomSummary: RoomSummary): RoomListRoomSummary {
        val roomInfo = roomSummary.info
        val avatarData = roomInfo.getAvatarData(size = AvatarSize.RoomListItem)
        return RoomListRoomSummary(
            id = roomSummary.roomId.value,
            roomId = roomSummary.roomId,
            name = roomInfo.name,
            numberOfUnreadMessages = roomInfo.numUnreadMessages,
            numberOfUnreadMentions = roomInfo.numUnreadMentions,
            numberOfUnreadNotifications = roomInfo.numUnreadNotifications,
            isMarkedUnread = roomInfo.isMarkedUnread,
            timestamp = dateFormatter.format(
                timestamp = roomSummary.latestEventTimestamp,
                mode = DateFormatterMode.TimeOrDate,
                useRelative = true,
            ),
            latestEvent = computeLatestEvent(roomSummary.latestEvent, roomInfo.hasOnlyTwoMembers()),
            avatarData = avatarData,
            userDefinedNotificationMode = roomInfo.userDefinedNotificationMode,
            hasRoomCall = roomInfo.hasRoomCall,
            activeCallIntent = when (val consensus = roomInfo.activeCallIntentConsensus) {
                is CallIntentConsensus.Full -> consensus.callIntent
                is CallIntentConsensus.Partial -> consensus.callIntent
                CallIntentConsensus.None -> null
            },
            isDirect = roomInfo.isDirect,
            isFavorite = roomInfo.isFavorite,
            inviteSender = roomInfo.inviter?.toInviteSender(),
            isDm = roomInfo.isDm,
            canonicalAlias = roomInfo.canonicalAlias,
            displayType = when (roomInfo.currentUserMembership) {
                CurrentUserMembership.INVITED -> {
                    RoomSummaryDisplayType.INVITE
                }
                CurrentUserMembership.KNOCKED -> {
                    RoomSummaryDisplayType.KNOCKED
                }
                else -> {
                    RoomSummaryDisplayType.ROOM
                }
            },
            heroes = roomInfo.heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomListItem)
            }.toImmutableList(),
            isTombstoned = roomInfo.successorRoom != null,
            isSpace = roomInfo.isSpace,
            dmUserStatus = roomInfo.dmUserStatus(),
        )
    }

    private fun RoomInfo.hasOnlyTwoMembers(): Boolean {
        return isDm || activeMembersCount <= 2
    }

    private fun computeLatestEvent(latestEvent: LatestEventValue, dm: Boolean): LatestEvent {
        return when (latestEvent) {
            is LatestEventValue.None -> {
                LatestEvent.None
            }
            is LatestEventValue.Local -> {
                if (latestEvent.isSending) {
                    val content = roomLatestEventFormatter.format(latestEvent, dm).orEmpty()
                    LatestEvent.Sending(
                        content = content,
                    )
                } else {
                    LatestEvent.Error
                }
            }
            is LatestEventValue.Remote -> {
                val content = roomLatestEventFormatter.format(latestEvent, dm).orEmpty()
                LatestEvent.Synced(
                    content = content,
                )
            }
            is LatestEventValue.RoomInvite -> LatestEvent.None
        }
    }
}
