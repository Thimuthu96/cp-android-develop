/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.libraries.matrix.impl.timeline

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiTimeline
import cloud.orelit.communicationplatform.libraries.matrix.impl.room.RoomContentForwarder
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.services.toolbox.api.systemclock.SystemClock
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.TimelineDiff
import uniffi.matrix_sdk.PaginationStatus
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

class RustTimelineTest {
    @Test
    fun `ensure that the timeline emits new loading item when pagination does not bring new events`() = runTest {
        val inner = FakeFfiTimeline()
        val systemClock = FakeSystemClock()
        val sut = createRustTimeline(
            inner = inner,
            systemClock = systemClock,
        )
        sut.timelineItems.test {
            // Give time for the listener to be set
            runCurrent()
            inner.emitDiff(
                listOf(
                    TimelineDiff.Reset(emptyList())
                )
            )
            with(awaitItem()) {
                assertThat(size).isEqualTo(2)
                // The loading
                assertThat((get(0) as MatrixTimelineItem.Virtual).virtual).isEqualTo(
                    VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.BACKWARDS,
                        timestamp = A_FAKE_TIMESTAMP,
                    )
                )
                // Typing notification
                assertThat((get(1) as MatrixTimelineItem.Virtual).virtual).isEqualTo(VirtualTimelineItem.TypingNotification)
            }
            systemClock.epochMillisResult = A_FAKE_TIMESTAMP + 1
            // Start pagination
            sut.paginate(Timeline.PaginationDirection.BACKWARDS)
            // Simulate SDK starting pagination
            inner.emitPaginationStatus(PaginationStatus.Paginating)
            // No new events received
            // Simulate SDK stopping pagination, more event to load
            inner.emitPaginationStatus(PaginationStatus.Idle(hitTimelineStart = false))
            // expect an item to be emitted, with an updated timestamp
            with(awaitItem()) {
                assertThat(size).isEqualTo(2)
                // The loading
                assertThat((get(0) as MatrixTimelineItem.Virtual).virtual).isEqualTo(
                    VirtualTimelineItem.LoadingIndicator(
                        direction = Timeline.PaginationDirection.BACKWARDS,
                        timestamp = A_FAKE_TIMESTAMP + 1,
                    )
                )
                // Typing notification
                assertThat((get(1) as MatrixTimelineItem.Virtual).virtual).isEqualTo(VirtualTimelineItem.TypingNotification)
            }
        }
    }
}

private fun TestScope.createRustTimeline(
    inner: InnerTimeline,
    mode: Timeline.Mode = Timeline.Mode.Live,
    systemClock: SystemClock = FakeSystemClock(),
    joinedRoom: JoinedRoom = FakeJoinedRoom().apply { givenRoomInfo(aRoomInfo()) },
    coroutineScope: CoroutineScope = backgroundScope,
    dispatcher: CoroutineDispatcher = testCoroutineDispatchers().io,
    roomContentForwarder: RoomContentForwarder = RoomContentForwarder(FakeFfiRoomListService()),
): RustTimeline {
    return RustTimeline(
        inner = inner,
        mode = mode,
        systemClock = systemClock,
        joinedRoom = joinedRoom,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher,
        roomContentForwarder = roomContentForwarder,
    )
}
