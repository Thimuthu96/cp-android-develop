/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.LAST_MESSAGE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ROOM_NAME
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.InviteSender
import kotlinx.collections.immutable.toImmutableList

open class RoomListRoomSummaryPreviewParam : PreviewParameterProvider<RoomListRoomSummary> {
    override val values: Sequence<RoomListRoomSummary>
        get() = sequenceOf(
            listOf(
                aRoomListRoomSummary(displayType = RoomSummaryDisplayType.PLACEHOLDER),
                aRoomListRoomSummary(),
                aRoomListRoomSummary(name = null),
                aRoomListRoomSummary(latestEvent = LatestEvent.None),
                aRoomListRoomSummary(
                    name = "A very long room name that should be truncated",
                    latestEvent = LatestEvent.Synced(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                            " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea com" +
                            "modo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
                    ),
                    timestamp = "yesterday",
                    numberOfUnreadMessages = 1,
                ),
            ),
            listOf(false, true).map { hasCall ->
                listOf(
                    RoomNotificationMode.ALL_MESSAGES,
                    RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
                    RoomNotificationMode.MUTE,
                ).map { roomNotificationMode ->
                    listOf(
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            latestEvent = LatestEvent.Synced("No activity" + if (hasCall) ", call" else ""),
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 0,
                            numberOfUnreadMentions = 0,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            latestEvent = LatestEvent.Synced("New messages" + if (hasCall) ", call" else ""),
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 1,
                            numberOfUnreadMentions = 0,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            latestEvent = LatestEvent.Synced("New messages, mentions" + if (hasCall) ", call" else ""),
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 1,
                            numberOfUnreadMentions = 1,
                            hasRoomCall = hasCall,
                        ),
                        aRoomListRoomSummary(
                            name = roomNotificationMode.name,
                            latestEvent = LatestEvent.Synced("New mentions" + if (hasCall) ", call" else ""),
                            notificationMode = roomNotificationMode,
                            numberOfUnreadMessages = 0,
                            numberOfUnreadMentions = 1,
                            hasRoomCall = hasCall,
                        ),
                    )
                }.flatten()
            }.flatten(),
            listOf(
                aRoomListRoomSummary(
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@alice:matrix.org"),
                        displayName = USER_NAME_ALICE,
                    ),
                    canonicalAlias = RoomAlias("#alias:matrix.org"),
                ),
                aRoomListRoomSummary(
                    name = USER_NAME_BOB,
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@bob:matrix.org"),
                        displayName = USER_NAME_BOB,
                    ),
                    isDm = true,
                ),
                aRoomListRoomSummary(
                    name = null,
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@bob:matrix.org"),
                        displayName = USER_NAME_BOB,
                    ),
                ),
                aRoomListRoomSummary(
                    name = "A space invite",
                    displayType = RoomSummaryDisplayType.INVITE,
                    inviteSender = anInviteSender(
                        userId = UserId("@bob:matrix.org"),
                        displayName = USER_NAME_BOB,
                    ),
                    isSpace = true
                ),
                aRoomListRoomSummary(
                    name = "A knocked room",
                    displayType = RoomSummaryDisplayType.KNOCKED,
                ),
                aRoomListRoomSummary(
                    name = "A knocked room with alias",
                    canonicalAlias = RoomAlias("#knockable:matrix.org"),
                    displayType = RoomSummaryDisplayType.KNOCKED,
                ),
                aRoomListRoomSummary(
                    name = "A tombstoned room",
                    displayType = RoomSummaryDisplayType.ROOM,
                    isTombstoned = true,
                )
            ),
            listOf(
                aRoomListRoomSummary(latestEvent = LatestEvent.Sending("A sending message")),
                aRoomListRoomSummary(latestEvent = LatestEvent.Error),
            ),
            listOf(
                aRoomListRoomSummary(
                    name = "Active voice call",
                    latestEvent = LatestEvent.Synced("No activity, call"),
                    hasRoomCall = true,
                    activeCallIntent = CallIntent.AUDIO
                ),
            )
        ).flatten()
}

internal fun anInviteSender(
    userId: UserId = UserId("@bob:domain"),
    displayName: String = USER_NAME_BOB,
    avatarData: AvatarData = AvatarData(userId.value, displayName, size = AvatarSize.InviteSender),
) = InviteSender(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
    membershipChangeReason = null,
)

internal fun aRoomListRoomSummary(
    id: String = "!roomId:domain",
    name: String? = ROOM_NAME,
    numberOfUnreadMessages: Long = 0,
    numberOfUnreadMentions: Long = 0,
    numberOfUnreadNotifications: Long = 0,
    isMarkedUnread: Boolean = false,
    latestEvent: LatestEvent = LatestEvent.Synced(LAST_MESSAGE),
    timestamp: String? = latestEvent.takeIf { it !is LatestEvent.None }?.let { "88:88" },
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    activeCallIntent: CallIntent? = null,
    avatarData: AvatarData = AvatarData(id, name, size = AvatarSize.RoomListItem),
    isDirect: Boolean = false,
    isDm: Boolean = false,
    isFavorite: Boolean = false,
    inviteSender: InviteSender? = null,
    displayType: RoomSummaryDisplayType = RoomSummaryDisplayType.ROOM,
    canonicalAlias: RoomAlias? = null,
    heroes: List<AvatarData> = emptyList(),
    isTombstoned: Boolean = false,
    isSpace: Boolean = false,
    dmUserStatus: DisplayedStatus? = null,
) = RoomListRoomSummary(
    id = id,
    roomId = RoomId(id),
    name = name,
    numberOfUnreadMessages = numberOfUnreadMessages,
    numberOfUnreadMentions = numberOfUnreadMentions,
    numberOfUnreadNotifications = numberOfUnreadNotifications,
    isMarkedUnread = isMarkedUnread,
    timestamp = timestamp,
    latestEvent = latestEvent,
    avatarData = avatarData,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    activeCallIntent = activeCallIntent,
    isDirect = isDirect,
    isDm = isDm,
    isFavorite = isFavorite,
    inviteSender = inviteSender,
    displayType = displayType,
    canonicalAlias = canonicalAlias,
    heroes = heroes.toImmutableList(),
    isTombstoned = isTombstoned,
    isSpace = isSpace,
    dmUserStatus = dmUserStatus,
)
