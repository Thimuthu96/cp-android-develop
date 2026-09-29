/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications.fixtures

import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.RtcNotificationType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventType
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_MESSAGE
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_TIMESTAMP
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_NAME_2
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.InviteNotifiableEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableMessageEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP

fun aSimpleNotifiableEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    type: String? = null,
    isRedacted: Boolean = false,
    canBeReplaced: Boolean = false,
    editedEventId: EventId? = null,
    noisy: Boolean = false,
) = SimpleNotifiableEvent(
    sessionId = sessionId,
    roomId = roomId,
    eventId = eventId,
    editedEventId = editedEventId,
    noisy = noisy,
    title = "title",
    description = "description",
    type = type,
    timestamp = 0,
    soundName = null,
    canBeReplaced = canBeReplaced,
    isRedacted = isRedacted
)

fun anInviteNotifiableEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    isRedacted: Boolean = false,
    noisy: Boolean = false,
) = InviteNotifiableEvent(
    sessionId = sessionId,
    eventId = eventId,
    roomId = roomId,
    roomName = A_ROOM_NAME,
    editedEventId = null,
    noisy = noisy,
    title = "title",
    description = "description",
    type = null,
    timestamp = 0,
    soundName = null,
    canBeReplaced = false,
    isRedacted = isRedacted
)

fun aNotifiableMessageEvent(
    body: String = A_MESSAGE,
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    threadId: ThreadId? = null,
    isRedacted: Boolean = false,
    hasMentionOrReply: Boolean = false,
    timestamp: Long = A_TIMESTAMP,
    type: String = EventType.MESSAGE,
    senderId: UserId = A_USER_ID_2,
    senderDisambiguatedDisplayName: String = A_USER_NAME_2,
    roomName: String? = A_ROOM_NAME,
    noisy: Boolean = false,
) = NotifiableMessageEvent(
    sessionId = sessionId,
    eventId = eventId,
    editedEventId = null,
    noisy = noisy,
    timestamp = timestamp,
    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
    senderId = senderId,
    body = body,
    roomId = roomId,
    threadId = threadId,
    roomName = roomName,
    canBeReplaced = false,
    isRedacted = isRedacted,
    imageUriString = null,
    imageMimeType = null,
    roomAvatarPath = null,
    senderAvatarPath = null,
    soundName = null,
    outGoingMessage = false,
    outGoingMessageFailed = false,
    isUpdated = false,
    hasMentionOrReply = hasMentionOrReply,
    type = type,
)

fun aNotifiableCallEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    senderId: UserId = A_USER_ID_2,
    senderName: String? = null,
    roomAvatarUrl: String? = AN_AVATAR_URL,
    senderAvatarUrl: String? = AN_AVATAR_URL,
    rtcNotificationType: RtcNotificationType = RtcNotificationType.NOTIFY,
    timestamp: Long = 0L,
    expirationTimestamp: Long = 0L,
    callIntent: CallIntent = CallIntent.VIDEO,
) = NotifiableRingingCallEvent(
    sessionId = sessionId,
    eventId = eventId,
    roomId = roomId,
    roomName = A_ROOM_NAME,
    editedEventId = null,
    description = "description",
    timestamp = timestamp,
    expirationTimestamp = expirationTimestamp,
    canBeReplaced = false,
    isRedacted = false,
    isUpdated = false,
    senderDisambiguatedDisplayName = senderName,
    senderId = senderId,
    roomAvatarUrl = roomAvatarUrl,
    senderAvatarUrl = senderAvatarUrl,
    rtcNotificationType = rtcNotificationType,
    callIntent = callIntent,
)

fun aFallbackNotifiableEvent(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    description: String? = "A fallback notification",
    canBeReplaced: Boolean = false,
    noisy: Boolean = false,
    timestamp: Long = A_FAKE_TIMESTAMP,
    cause: String? = "Unable to decrypt event",
) = FallbackNotifiableEvent(
    sessionId = sessionId,
    roomId = roomId,
    eventId = eventId,
    editedEventId = null,
    description = description,
    canBeReplaced = canBeReplaced,
    isRedacted = false,
    isUpdated = false,
    noisy = noisy,
    timestamp = timestamp,
    cause = cause,
)
