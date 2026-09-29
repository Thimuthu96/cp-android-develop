/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.aNewEventForm
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembersState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomMember
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class DefaultUpdateCalendarEventActionTest {
    private val berlin = ZoneId.of("Europe/Berlin")
    private val alice = aMatrixUser(id = "@alice:example.org")
    private val bob = aMatrixUser(id = "@bob:example.org")
    private val carol = aMatrixUser(id = "@carol:example.org")
    private val dave = aMatrixUser(id = "@dave:example.org")

    /** The participants the form was opened with. */
    private val initialParticipants = listOf(alice.userId, bob.userId)

    // Every Thursday at 14:00, with one occurrence cancelled and one moved.
    private val cancelledOccurrence = LocalDateTime.of(2026, 10, 1, 14, 0)
    private val movedOccurrence = LocalDateTime.of(2026, 10, 8, 14, 0)
    private val series = CalendarSchedule(
        start = LocalDateTime.of(2026, 9, 24, 14, 0),
        end = LocalDateTime.of(2026, 9, 24, 15, 0),
        timeZone = berlin,
        isAllDay = false,
        rrule = "FREQ=WEEKLY;BYDAY=TH",
        cancelledOccurrences = setOf(cancelledOccurrence),
        overrides = mapOf(movedOccurrence to OccurrenceOverride(start = movedOccurrence.plusHours(1), end = movedOccurrence.plusHours(2))),
    )

    @Test
    fun `cancelling one occurrence keeps the other exceptions`() = runTest {
        val fixture = Fixture()
        val occurrence = LocalDateTime.of(2026, 10, 15, 14, 0)

        val result = fixture.action().cancel(A_ROOM_ID, occurrence)

        assertThat(result.isSuccess).isTrue()
        val written = fixture.writtenSchedule()
        assertThat(written.cancelledOccurrences).containsExactly(cancelledOccurrence, occurrence)
        assertThat(written.overrides).isEqualTo(series.overrides)
        assertThat(written.isCancelled).isFalse()
        assertThat(CalendarRoomEncoding.decodeDescription(fixture.topics.single())).isEqualTo("Plan the sprint")
    }

    @Test
    fun `cancelling the whole event marks it cancelled and keeps the room`() = runTest {
        val fixture = Fixture()

        fixture.action().cancel(A_ROOM_ID, null).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.isCancelled).isTrue()
        assertThat(written.cancelledOccurrences).isEqualTo(series.cancelledOccurrences)
        assertThat(written.overrides).isEqualTo(series.overrides)
    }

    @Test
    fun `a failure to write the topic fails the cancellation`() = runTest {
        val fixture = Fixture(setTopicResult = { Result.failure(AN_EXCEPTION) })

        val result = fixture.action().cancel(A_ROOM_ID, null)

        assertThat(result.exceptionOrNull()).isEqualTo(AN_EXCEPTION)
    }

    @Test
    fun `a room without an event cannot be changed`() = runTest {
        val fixture = Fixture(topic = "Just a room")

        val result = fixture.action().cancel(A_ROOM_ID, null)

        assertThat(result.exceptionOrNull()).isInstanceOf(NotACalendarEventException::class.java)
        assertThat(fixture.topics).isEmpty()
    }

    @Test
    fun `editing an occurrence adds an override and keeps the other exceptions`() = runTest {
        val fixture = Fixture()
        val occurrence = LocalDateTime.of(2026, 10, 15, 14, 0)
        val form = aNewEventForm(
            title = "Special review",
            description = "Plan the sprint",
            start = LocalDateTime.of(2026, 10, 16, 9, 0),
            end = LocalDateTime.of(2026, 10, 16, 10, 0),
        )

        fixture.action().editOccurrence(A_ROOM_ID, occurrence, form, berlin).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.overrides[occurrence]).isEqualTo(
            OccurrenceOverride(
                start = LocalDateTime.of(2026, 10, 16, 9, 0),
                end = LocalDateTime.of(2026, 10, 16, 10, 0),
                title = "Special review",
                // Unchanged, so it keeps following the series.
                description = null,
            )
        )
        assertThat(written.overrides[movedOccurrence]).isEqualTo(series.overrides[movedOccurrence])
        assertThat(written.cancelledOccurrences).isEqualTo(series.cancelledOccurrences)
        assertThat(written.rrule).isEqualTo(series.rrule)
        // Neither the name nor the participants of the series change.
        assertThat(fixture.names).isEmpty()
        assertThat(fixture.invited).isEmpty()
        assertThat(fixture.kicked).isEmpty()
    }

    @Test
    fun `editing the series without moving it keeps the exceptions`() = runTest {
        val fixture = Fixture()

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(title = "Renamed review"), initialParticipants, berlin).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.cancelledOccurrences).isEqualTo(series.cancelledOccurrences)
        assertThat(written.overrides).isEqualTo(series.overrides)
        assertThat(fixture.names).containsExactly("Renamed review")
    }

    @Test
    fun `editing the series from another time zone keeps its zone and its exceptions`() = runTest {
        val fixture = Fixture()
        // The same 14:00 Berlin, as a device in UTC shows it.
        val form = aSeriesForm(start = LocalDateTime.of(2026, 9, 24, 12, 0), end = LocalDateTime.of(2026, 9, 24, 13, 0))

        fixture.action().editSeries(A_ROOM_ID, form, initialParticipants, ZoneId.of("UTC")).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.timeZone).isEqualTo(berlin)
        assertThat(written.start).isEqualTo(series.start)
        assertThat(written.overrides).isEqualTo(series.overrides)
    }

    @Test
    fun `moving the series drops the exceptions`() = runTest {
        val fixture = Fixture()
        val form = aSeriesForm(start = LocalDateTime.of(2026, 9, 24, 16, 0), end = LocalDateTime.of(2026, 9, 24, 17, 0))

        fixture.action().editSeries(A_ROOM_ID, form, initialParticipants, berlin).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.start).isEqualTo(LocalDateTime.of(2026, 9, 24, 16, 0))
        assertThat(written.cancelledOccurrences).isEmpty()
        assertThat(written.overrides).isEmpty()
    }

    @Test
    fun `changing the repeat rule drops the exceptions`() = runTest {
        val fixture = Fixture()

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(recurrence = EventRecurrence.Daily), initialParticipants, berlin).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.rrule).isEqualTo("FREQ=DAILY")
        assertThat(written.cancelledOccurrences).isEmpty()
        assertThat(written.overrides).isEmpty()
    }

    @Test
    fun `editing the series invites the added people and removes the removed ones`() = runTest {
        val fixture = Fixture(members = listOf(alice.userId, bob.userId))

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(participants = listOf(alice, carol)), initialParticipants, berlin).getOrThrow()

        assertThat(fixture.invited).containsExactly(carol.userId)
        assertThat(fixture.kicked).containsExactly(bob.userId)
    }

    @Test
    fun `a participant who cannot be invited does not fail the edit but is reported`() = runTest {
        val fixture = Fixture(members = listOf(alice.userId), inviteResult = { Result.failure(AN_EXCEPTION) })

        val result = fixture.action().editSeries(A_ROOM_ID, aSeriesForm(participants = listOf(alice, carol)), initialParticipants, berlin)

        assertThat(fixture.invited).containsExactly(carol.userId)
        assertThat(result.getOrThrow().notUpdated).containsExactly(carol.userId)
    }

    @Test
    fun `only people the form was opened with and that were taken out are removed`() = runTest {
        // Dave joined after the form was opened, the organiser never saw him.
        val fixture = Fixture(members = listOf(alice.userId, bob.userId, dave.userId))

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(participants = listOf(alice)), initialParticipants, berlin).getOrThrow()

        assertThat(fixture.kicked).containsExactly(bob.userId)
        assertThat(fixture.invited).isEmpty()
    }

    @Test
    fun `the organiser is never removed, even when listed as taken out`() = runTest {
        val fixture = Fixture()

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(participants = listOf(alice, bob)), initialParticipants + A_SESSION_ID, berlin).getOrThrow()

        assertThat(fixture.kicked).isEmpty()
    }

    @Test
    fun `a failed rename still updates the participants and then reports the failure`() = runTest {
        val fixture = Fixture(setNameResult = { Result.failure(AN_EXCEPTION) })

        val result = fixture.action().editSeries(A_ROOM_ID, aSeriesForm(title = "Renamed", participants = listOf(alice, carol)), initialParticipants, berlin)

        assertThat(result.exceptionOrNull()).isEqualTo(AN_EXCEPTION)
        assertThat(fixture.topics).hasSize(1)
        assertThat(fixture.invited).containsExactly(carol.userId)
        assertThat(fixture.kicked).containsExactly(bob.userId)
    }

    @Test
    fun `a rule this app does not write is kept word for word when Repeat is unchanged`() = runTest {
        val rule = "FREQ=WEEKLY;INTERVAL=2;BYDAY=TH"
        val fixture = Fixture(topic = CalendarRoomEncoding.encode("Plan the sprint", series.copy(rrule = rule)))

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(title = "Renamed"), initialParticipants, berlin).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.rrule).isEqualTo(rule)
        assertThat(written.overrides).isEqualTo(series.overrides)
        assertThat(written.cancelledOccurrences).isEqualTo(series.cancelledOccurrences)
    }

    @Test
    fun `a rule this app does not write is replaced when another Repeat option is picked`() = runTest {
        val fixture = Fixture(topic = CalendarRoomEncoding.encode("Plan the sprint", series.copy(rrule = "FREQ=WEEKLY;INTERVAL=2;BYDAY=TH")))

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(recurrence = EventRecurrence.Daily), initialParticipants, berlin).getOrThrow()

        assertThat(fixture.writtenSchedule().rrule).isEqualTo("FREQ=DAILY")
    }

    @Test
    fun `an occurrence edited from another time zone is stored on the wall clock of the series`() = runTest {
        val fixture = Fixture()
        val occurrence = LocalDateTime.of(2026, 10, 15, 14, 0)
        // 09:00 to 10:00 UTC is 11:00 to 12:00 in Berlin in October.
        val form = aNewEventForm(
            title = "Design review",
            description = "Plan the sprint",
            start = LocalDateTime.of(2026, 10, 15, 9, 0),
            end = LocalDateTime.of(2026, 10, 15, 10, 0),
        )

        fixture.action().editOccurrence(A_ROOM_ID, occurrence, form, ZoneId.of("UTC")).getOrThrow()

        val written = fixture.writtenSchedule()
        assertThat(written.timeZone).isEqualTo(berlin)
        assertThat(written.overrides[occurrence]).isEqualTo(
            OccurrenceOverride(start = LocalDateTime.of(2026, 10, 15, 11, 0), end = LocalDateTime.of(2026, 10, 15, 12, 0))
        )
    }

    @Test
    fun `nobody is removed when the member list is not known`() = runTest {
        val fixture = Fixture(members = null)

        fixture.action().editSeries(A_ROOM_ID, aSeriesForm(participants = listOf(carol)), initialParticipants, berlin).getOrThrow()

        assertThat(fixture.invited).containsExactly(carol.userId)
        assertThat(fixture.kicked).isEmpty()
    }

    private fun aSeriesForm(
        title: String = "Design review",
        start: LocalDateTime = series.start,
        end: LocalDateTime = series.end,
        recurrence: EventRecurrence = EventRecurrence.Weekly,
        participants: List<MatrixUser> = listOf(alice, bob),
    ) = aNewEventForm(
        title = title,
        description = "Plan the sprint",
        start = start,
        end = end,
        recurrence = recurrence,
        participants = participants,
    )

    private inner class Fixture(
        topic: String = CalendarRoomEncoding.encode("Plan the sprint", series),
        /** The user ids in the room besides the organiser, null when the member list could not be loaded. */
        members: List<UserId>? = listOf(alice.userId, bob.userId),
        setTopicResult: (String) -> Result<Unit> = { Result.success(Unit) },
        setNameResult: (String) -> Result<Unit> = { Result.success(Unit) },
        inviteResult: (UserId) -> Result<Unit> = { Result.success(Unit) },
    ) {
        val topics = mutableListOf<String>()
        val names = mutableListOf<String>()
        val invited = mutableListOf<UserId>()
        val kicked = mutableListOf<UserId>()

        private val baseRoom = FakeBaseRoom(
            roomId = A_ROOM_ID,
            initialRoomInfo = aRoomInfo(id = A_ROOM_ID, name = "Design review", topic = topic),
            updateMembersResult = {},
        ).apply {
            if (members != null) {
                val roomMembers = (members + A_SESSION_ID).map { aRoomMember(userId = it, membership = RoomMembershipState.JOIN) }
                givenRoomMembersState(RoomMembersState.Ready(roomMembers.toImmutableList()))
            }
        }

        private val room = FakeJoinedRoom(
            baseRoom = baseRoom,
            setTopicResult = {
                topics += it
                setTopicResult(it)
            },
            setNameResult = {
                names += it
                setNameResult(it)
            },
            inviteUserResult = {
                invited += it
                inviteResult(it)
            },
            kickUserResult = { userId, _ ->
                kicked += userId
                Result.success(Unit)
            },
        )

        fun action() = DefaultUpdateCalendarEventAction(
            client = FakeMatrixClient().apply { givenGetRoomResult(A_ROOM_ID, room) },
        )

        fun writtenSchedule(): CalendarSchedule = CalendarRoomEncoding.decodeSchedule(topics.single())!!
    }
}
