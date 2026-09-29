/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomSummary
import cloud.orelit.communicationplatform.libraries.matrix.test.roomlist.FakeDynamicRoomList
import cloud.orelit.communicationplatform.libraries.matrix.test.roomlist.FakeRoomListService
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DefaultCalendarEventRepositoryTest {
    private val roomId = RoomId("!event:example.org")
    private val organizer = UserId("@alice:example.org")

    // Every Thursday at 14:00 on this device's clock, so the test does not depend on where it runs.
    private val moved = LocalDateTime.of(2026, 10, 1, 14, 0)
    private val cancelled = LocalDateTime.of(2026, 10, 8, 14, 0)
    private val series = CalendarSchedule(
        start = LocalDateTime.of(2026, 9, 24, 14, 0),
        end = LocalDateTime.of(2026, 9, 24, 15, 0),
        timeZone = ZoneId.systemDefault(),
        isAllDay = false,
        rrule = "FREQ=WEEKLY;BYDAY=TH",
        cancelledOccurrences = setOf(cancelled),
        overrides = mapOf(
            moved to OccurrenceOverride(
                start = LocalDateTime.of(2026, 10, 2, 16, 0),
                end = LocalDateTime.of(2026, 10, 2, 17, 0),
                title = "Moved review",
            )
        ),
    )

    @Test
    fun `the exceptions of a series are applied to its entries`() = runTest {
        val entries = aRepository().entriesFlow(around = LocalDate.of(2026, 10, 1), daysBefore = 7, daysAfter = 10).first()

        assertThat(entries.map { it.occurrenceStart }).containsExactly(series.start, moved, cancelled)

        val movedEntry = entries.single { it.occurrenceStart == moved }
        // Keyed on where the occurrence was, not where it moved to, so an open sheet keeps following it.
        assertThat(movedEntry.id).isEqualTo("${roomId.value}@2026-10-01")
        assertThat(movedEntry.start).isEqualTo(LocalDateTime.of(2026, 10, 2, 16, 0))
        assertThat(movedEntry.end).isEqualTo(LocalDateTime.of(2026, 10, 2, 17, 0))
        assertThat(movedEntry.title).isEqualTo("Moved review")
        assertThat(movedEntry.description).isEqualTo("Plan the sprint")
        assertThat(movedEntry.isCancelled).isFalse()

        val cancelledEntry = entries.single { it.occurrenceStart == cancelled }
        assertThat(cancelledEntry.isCancelled).isTrue()
        assertThat(cancelledEntry.title).isEqualTo("Design review")
        assertThat(cancelledEntry.id).isEqualTo("${roomId.value}@2026-10-08")

        val untouched = entries.single { it.occurrenceStart == series.start }
        assertThat(untouched.isCancelled).isFalse()
        assertThat(untouched.start).isEqualTo(series.start)
        assertThat(untouched.room!!.organizer).isEqualTo(organizer)
    }

    @Test
    fun `a cancelled series is cancelled on every entry`() = runTest {
        val entries = aRepository(series.copy(isCancelled = true))
            .entriesFlow(around = LocalDate.of(2026, 10, 1), daysBefore = 7, daysAfter = 10)
            .first()

        assertThat(entries).hasSize(3)
        assertThat(entries.all { it.isCancelled }).isTrue()
    }

    private fun TestScope.aRepository(schedule: CalendarSchedule = series): DefaultCalendarEventRepository {
        val summary = aRoomSummary(
            roomId = roomId,
            name = "Design review",
            topic = CalendarRoomEncoding.encode("Plan the sprint", schedule),
            roomCreators = listOf(organizer),
        )
        val roomList = FakeDynamicRoomList(summaries = MutableStateFlow(listOf(summary)))
        return DefaultCalendarEventRepository(
            client = FakeMatrixClient(roomListService = FakeRoomListService(allRooms = roomList)),
            coroutineDispatchers = testCoroutineDispatchers(useUnconfinedTestDispatcher = true),
        )
    }
}
