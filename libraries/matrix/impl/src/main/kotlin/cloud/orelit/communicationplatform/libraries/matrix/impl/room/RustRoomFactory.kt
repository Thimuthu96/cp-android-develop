/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.room

import cloud.orelit.communicationplatform.appconfig.TimelineConfig
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.matrix.api.core.DeviceId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.notificationsettings.NotificationSettingsService
import cloud.orelit.communicationplatform.libraries.matrix.api.room.BaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipObserver
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomListService
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.awaitLoaded
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.join.map
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.preview.RoomPreviewInfoMapper
import cloud.orelit.communicationplatform.libraries.matrix.impl.roomlist.roomOrNull
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.api.recordTransaction
import cloud.orelit.communicationplatform.services.analyticsproviders.api.recordChildTransaction
import cloud.orelit.communicationplatform.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.DateDividerMode
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.TimelineConfiguration
import org.matrix.rustcomponents.sdk.TimelineFilter
import org.matrix.rustcomponents.sdk.TimelineFocus
import timber.log.Timber
import uniffi.matrix_sdk_base.EncryptionState
import uniffi.matrix_sdk_ui.TimelineReadReceiptTracking
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

class RustRoomFactory(
    private val sessionId: SessionId,
    private val deviceId: DeviceId,
    private val notificationSettingsService: NotificationSettingsService,
    private val sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val roomListService: RoomListService,
    private val innerRoomListService: InnerRoomListService,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    private val timelineEventFilterFactory: TimelineEventFilterFactory,
    private val featureFlagService: FeatureFlagService,
    private val roomMembershipObserver: RoomMembershipObserver,
    private val roomInfoMapper: RoomInfoMapper,
    private val analyticsService: AnalyticsService,
) {
    private val dispatcher = dispatchers.computation.limitedParallelism(1)
    private val mutex = Mutex()
    private val isDestroyed: AtomicBoolean = AtomicBoolean(false)

    suspend fun destroy() {
        withContext(NonCancellable + dispatcher) {
            mutex.withLock {
                Timber.d("Destroying room factory")
                isDestroyed.set(true)
            }
        }
    }

    suspend fun getBaseRoom(roomId: RoomId): RustBaseRoom? = withContext(dispatcher) {
        mutex.withLock {
            if (isDestroyed.get()) {
                Timber.d("Room factory is destroyed, returning null for $roomId")
                return@withContext null
            }
            val room = awaitRoomInRoomList(roomId) ?: return@withContext null
            getBaseRoom(sdkRoom = room, roomInfo = room.roomInfo())
        }
    }

    private fun getBaseRoom(sdkRoom: Room, roomInfo: RoomInfo) = RustBaseRoom(
        sessionId = sessionId,
        deviceId = deviceId,
        innerRoom = sdkRoom,
        coroutineDispatchers = dispatchers,
        roomSyncSubscriber = roomSyncSubscriber,
        roomMembershipObserver = roomMembershipObserver,
        roomInfoMapper = roomInfoMapper,
        initialRoomInfo = roomInfoMapper.map(roomInfo),
        sessionCoroutineScope = sessionCoroutineScope,
    )

    suspend fun getJoinedRoomOrPreview(roomId: RoomId, serverNames: List<String>): GetRoomResult? = withContext(dispatcher) {
        mutex.withLock {
            if (isDestroyed.get()) {
                Timber.d("Room factory is destroyed, returning null for $roomId")
                return@withContext null
            }

            val sdkRoom = awaitRoomInRoomList(roomId) ?: return@withLock null
            val roomInfo = sdkRoom.roomInfo()

            val parentTransaction = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.OpenRoom)

            if (roomInfo.membership == Membership.JOINED) {
                analyticsService.recordTransaction(
                    name = "Get joined room",
                    operation = "RustRoomFactory.getJoinedRoomOrPreview",
                    parentTransaction = parentTransaction,
                ) { transaction ->
                    val hideThreadedEvents = featureFlagService.isFeatureEnabled(FeatureFlags.Threads)
                    // Init the live timeline in the SDK from the Room
                    val timeline = transaction.recordChildTransaction(
                        operation = "sdkRoom.timelineWithConfiguration",
                        description = "Get timeline from the SDK",
                    ) {
                        val isEncrypted = when (roomInfo.encryptionState) {
                            EncryptionState.ENCRYPTED -> true
                            EncryptionState.NOT_ENCRYPTED -> false
                            EncryptionState.UNKNOWN -> null
                        }
                        val timelineFilter = timelineEventFilterFactory.create(
                            joinRule = roomInfo.joinRule?.map(),
                            isEncrypted = isEncrypted,
                            excludedStateTypes = TimelineConfig.excludedEvents,
                        )
                        sdkRoom.timelineWithConfiguration(
                            TimelineConfiguration(
                                focus = TimelineFocus.Live(hideThreadedEvents = hideThreadedEvents),
                                filter = timelineFilter?.let(TimelineFilter::EventFilter) ?: TimelineFilter.All,
                                internalIdPrefix = "live",
                                dateDividerMode = DateDividerMode.DAILY,
                                trackReadReceipts = TimelineReadReceiptTracking.MESSAGE_LIKE_EVENTS,
                                reportUtds = true,
                            )
                        )
                    }

                    GetRoomResult.Joined(
                        JoinedRustRoom(
                            baseRoom = getBaseRoom(sdkRoom, roomInfo),
                            notificationSettingsService = notificationSettingsService,
                            roomContentForwarder = roomContentForwarder,
                            liveInnerTimeline = timeline,
                            coroutineDispatchers = dispatchers,
                            systemClock = systemClock,
                            featureFlagService = featureFlagService,
                        )
                    )
                }
            } else {
                analyticsService.recordTransaction(
                    name = "Get preview of room",
                    operation = "RustRoomFactory.getJoinedRoomOrPreview",
                    parentTransaction = parentTransaction,
                ) {
                    val preview = try {
                        sdkRoom.previewRoom(via = serverNames)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to get room preview for $roomId")
                        return@recordTransaction null
                    }

                    GetRoomResult.NotJoined(
                        NotJoinedRustRoom(
                            sessionId = sessionId,
                            localRoom = getBaseRoom(sdkRoom, roomInfo),
                            previewInfo = RoomPreviewInfoMapper.map(preview.info()),
                        )
                    )
                }
            }
        }
    }

    /**
     * Get the Rust room for a room, retrying after the room list is loaded if necessary.
     */
    private suspend fun awaitRoomInRoomList(roomId: RoomId): Room? {
        var sdkRoom = innerRoomListService.roomOrNull(roomId.value)
        if (sdkRoom == null) {
            // ... otherwise, lets wait for the SS to load all rooms and check again.
            roomListService.allRooms.awaitLoaded()
            sdkRoom = innerRoomListService.roomOrNull(roomId.value)
        }

        if (sdkRoom == null) {
            Timber.d("Room not found for $roomId")
            return null
        }

        return sdkRoom
    }
}

sealed interface GetRoomResult {
    data class Joined(val joinedRoom: JoinedRoom) : GetRoomResult
    data class NotJoined(val notJoinedRoom: NotJoinedRustRoom) : GetRoomResult

    val room: BaseRoom?
        get() = when (this) {
            is Joined -> joinedRoom
            is NotJoined -> notJoinedRoom.localRoom
        }
}
