/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.room

import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CallIntentConsensus
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode
import cloud.orelit.communicationplatform.libraries.matrix.api.room.history.RoomHistoryVisibility
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import cloud.orelit.communicationplatform.libraries.matrix.api.room.tombstone.SuccessorRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_RAW_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_TOPIC
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

fun aRoomInfo(
    id: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    rawName: String? = A_ROOM_RAW_NAME,
    topic: String? = A_ROOM_TOPIC,
    avatarUrl: String? = AN_AVATAR_URL,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    isEncrypted: Boolean = false,
    joinRule: JoinRule? = JoinRule.Public,
    isSpace: Boolean = false,
    successorRoom: SuccessorRoom? = null,
    isFavorite: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    alternativeAliases: List<RoomAlias> = emptyList(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 2,
    invitedMembersCount: Long = 1,
    joinedMembersCount: Long = 1,
    highlightCount: Long = 0,
    notificationCount: Long = 0,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    roomPowerLevels: RoomPowerLevels? = RoomPowerLevels(
        values = defaultRoomPowerLevelValues(),
        users = persistentMapOf(),
    ),
    activeRoomCallParticipants: List<UserId> = emptyList(),
    heroes: List<MatrixUser> = emptyList(),
    pinnedEventIds: List<EventId> = emptyList(),
    roomCreators: List<UserId> = emptyList(),
    isMarkedUnread: Boolean = false,
    numUnreadMessages: Long = 0,
    numUnreadNotifications: Long = 0,
    numUnreadMentions: Long = 0,
    historyVisibility: RoomHistoryVisibility = RoomHistoryVisibility.Joined,
    roomVersion: String? = "11",
    privilegedCreatorRole: Boolean = false,
    isLowPriority: Boolean = false,
    activeCallIntentConsensus: CallIntentConsensus = CallIntentConsensus.None,
    isDm: Boolean = false,
    fullyReadEventId: EventId? = null,
) = RoomInfo(
    id = id,
    name = name,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    isPublic = isPublic,
    isDirect = isDirect,
    isEncrypted = isEncrypted,
    joinRule = joinRule,
    isSpace = isSpace,
    successorRoom = successorRoom,
    isFavorite = isFavorite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases.toImmutableList(),
    currentUserMembership = currentUserMembership,
    inviter = inviter,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    roomPowerLevels = roomPowerLevels,
    activeRoomCallParticipants = activeRoomCallParticipants.toImmutableList(),
    heroes = heroes.toImmutableList(),
    pinnedEventIds = pinnedEventIds.toImmutableList(),
    creators = roomCreators.toImmutableList(),
    isMarkedUnread = isMarkedUnread,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    numUnreadMentions = numUnreadMentions,
    historyVisibility = historyVisibility,
    roomVersion = roomVersion,
    privilegedCreatorRole = privilegedCreatorRole,
    isLowPriority = isLowPriority,
    activeCallIntentConsensus = activeCallIntentConsensus,
    isDm = isDm,
    fullyReadEventId = fullyReadEventId,
)
