/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class CalendarRoomEncodingTest {
    @Test
    fun `a schedule survives a round trip`() {
        val topic = CalendarRoomEncoding.encode(description = "Plan the sprint", schedule = aSchedule())

        assertThat(CalendarRoomEncoding.decodeDescription(topic)).isEqualTo("Plan the sprint")
        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isEqualTo(aSchedule())
    }

    @Test
    fun `the description is what a user reads, the payload stays out of the way`() {
        val topic = CalendarRoomEncoding.encode(description = "Plan the sprint", schedule = aSchedule())

        assertThat(topic.substringBefore(CalendarRoomEncoding.SEPARATOR)).isEqualTo("Plan the sprint")
    }

    @Test
    fun `an empty description decodes to null rather than a blank string`() {
        val topic = CalendarRoomEncoding.encode(description = "   ", schedule = aSchedule())

        assertThat(CalendarRoomEncoding.decodeDescription(topic)).isNull()
        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isNotNull()
    }

    @Test
    fun `an all day schedule and its rule survive a round trip`() {
        val schedule = aSchedule(
            start = LocalDateTime.of(2026, 9, 24, 0, 0),
            end = LocalDateTime.of(2026, 9, 26, 0, 0),
            isAllDay = true,
            rrule = "FREQ=WEEKLY;BYDAY=TH",
        )

        val decoded = CalendarRoomEncoding.decodeSchedule(CalendarRoomEncoding.encode("", schedule))

        assertThat(decoded).isEqualTo(schedule)
    }

    @Test
    fun `a topic that is not an event has no schedule`() {
        assertThat(CalendarRoomEncoding.decodeSchedule(null)).isNull()
        assertThat(CalendarRoomEncoding.decodeSchedule("")).isNull()
        assertThat(CalendarRoomEncoding.decodeSchedule("Just a normal room topic")).isNull()
    }

    @Test
    fun `a malformed payload is ignored instead of crashing`() {
        val cases = listOf(
            "Topic${CalendarRoomEncoding.SEPARATOR}",
            "Topic${CalendarRoomEncoding.SEPARATOR}not json at all",
            "Topic${CalendarRoomEncoding.SEPARATOR}{}",
            // No version.
            """Topic${CalendarRoomEncoding.SEPARATOR}{"start":"20260924T140000","end":"20260924T150000"}""",
            // Unparseable times.
            """Topic${CalendarRoomEncoding.SEPARATOR}{"v":1,"start":"nope","end":"20260924T150000"}""",
            // The end is before the start.
            """Topic${CalendarRoomEncoding.SEPARATOR}{"v":1,"start":"20260924T150000","end":"20260924T140000"}""",
        )

        cases.forEach { topic ->
            assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isNull()
        }
    }

    @Test
    fun `a payload from a newer schema is ignored rather than misread`() {
        val topic = """Topic${CalendarRoomEncoding.SEPARATOR}{"v":99,"start":"20260924T140000","end":"20260924T150000"}"""

        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isNull()
    }

    @Test
    fun `an unknown time zone falls back to the device one instead of dropping the event`() {
        val topic = """Topic${CalendarRoomEncoding.SEPARATOR}{"v":1,"start":"20260924T140000","end":"20260924T150000","tz":"Mars/Olympus"}"""

        val decoded = CalendarRoomEncoding.decodeSchedule(topic)

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.timeZone).isEqualTo(ZoneId.systemDefault())
    }

    @Test
    fun `a separator pasted into the description cannot break the payload`() {
        val description = "Plan${CalendarRoomEncoding.SEPARATOR} the sprint"

        val topic = CalendarRoomEncoding.encode(description = description, schedule = aSchedule())

        assertThat(CalendarRoomEncoding.decodeDescription(topic)).isEqualTo("Plan the sprint")
        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isEqualTo(aSchedule())
    }

    @Test
    fun `a cancelled event survives a round trip and is written as version 2`() {
        val schedule = aSchedule(isCancelled = true)

        val topic = CalendarRoomEncoding.encode("", schedule)

        assertThat(topic).contains("\"v\":2")
        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isEqualTo(schedule)
    }

    @Test
    fun `cancelled occurrences and overrides survive a round trip`() {
        val schedule = aSchedule(
            rrule = "FREQ=WEEKLY;BYDAY=TH",
            cancelledOccurrences = setOf(LocalDateTime.of(2026, 10, 1, 14, 0)),
            overrides = mapOf(
                LocalDateTime.of(2026, 10, 8, 14, 0) to OccurrenceOverride(
                    start = LocalDateTime.of(2026, 10, 9, 16, 0),
                    end = LocalDateTime.of(2026, 10, 9, 17, 30),
                    title = "Moved review",
                    description = "",
                ),
                LocalDateTime.of(2026, 10, 15, 14, 0) to OccurrenceOverride(
                    start = LocalDateTime.of(2026, 10, 15, 15, 0),
                    end = LocalDateTime.of(2026, 10, 15, 16, 0),
                ),
            ),
        )

        val topic = CalendarRoomEncoding.encode("Plan the sprint", schedule)

        assertThat(topic).contains("\"v\":2")
        assertThat(CalendarRoomEncoding.decodeSchedule(topic)).isEqualTo(schedule)
        assertThat(CalendarRoomEncoding.decodeDescription(topic)).isEqualTo("Plan the sprint")
    }

    @Test
    fun `a schedule without exceptions is still written as version 1 so older clients show it`() {
        val topic = CalendarRoomEncoding.encode("", aSchedule(rrule = "FREQ=DAILY"))

        assertThat(topic).contains("\"v\":1")
        assertThat(topic).doesNotContain("cancelled")
        assertThat(topic).doesNotContain("overrides")
    }

    @Test
    fun `a version 1 payload is read as not cancelled and without exceptions`() {
        val topic = """Topic${CalendarRoomEncoding.SEPARATOR}{"v":1,"start":"20260924T140000","end":"20260924T150000","tz":"Europe/Berlin","allDay":false}"""

        val decoded = CalendarRoomEncoding.decodeSchedule(topic)!!

        assertThat(decoded.isCancelled).isFalse()
        assertThat(decoded.cancelledOccurrences).isEmpty()
        assertThat(decoded.overrides).isEmpty()
    }

    @Test
    fun `a version 2 payload with unknown keys and a malformed override keeps the rest`() {
        val topic = "Topic${CalendarRoomEncoding.SEPARATOR}" +
            """{"v":2,"start":"20260924T140000","end":"20260924T150000","tz":"Europe/Berlin","allDay":false,""" +
            """"rrule":"FREQ=DAILY","cancelled":false,"cancelledOn":["20260925T140000","nope"],"somethingNew":{"a":1},""" +
            """"overrides":[{"orig":"20260926T140000","start":"20260926T160000","end":"20260926T170000"},{"orig":"bad"},42]}"""

        val decoded = CalendarRoomEncoding.decodeSchedule(topic)!!

        assertThat(decoded.rrule).isEqualTo("FREQ=DAILY")
        assertThat(decoded.cancelledOccurrences).containsExactly(LocalDateTime.of(2026, 9, 25, 14, 0))
        assertThat(decoded.overrides.keys).containsExactly(LocalDateTime.of(2026, 9, 26, 14, 0))
        assertThat(decoded.overrides.values.single().title).isNull()
    }

    private fun aSchedule(
        start: LocalDateTime = LocalDateTime.of(2026, 9, 24, 14, 0),
        end: LocalDateTime = LocalDateTime.of(2026, 9, 24, 15, 0),
        timeZone: ZoneId = ZoneId.of("Europe/Berlin"),
        isAllDay: Boolean = false,
        rrule: String? = null,
        isCancelled: Boolean = false,
        cancelledOccurrences: Set<LocalDateTime> = emptySet(),
        overrides: Map<LocalDateTime, OccurrenceOverride> = emptyMap(),
    ) = CalendarSchedule(
        start = start,
        end = end,
        timeZone = timeZone,
        isAllDay = isAllDay,
        rrule = rrule,
        isCancelled = isCancelled,
        cancelledOccurrences = cancelledOccurrences,
        overrides = overrides,
    )
}
