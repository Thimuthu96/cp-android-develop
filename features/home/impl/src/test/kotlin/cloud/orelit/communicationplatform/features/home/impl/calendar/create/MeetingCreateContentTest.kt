/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Checks the message against the schema of CalendarEntryDto and MeetingCreateDto of the bot of matrix-meetings. */
class MeetingCreateContentTest {
    private val monday = LocalDate.of(2026, 9, 21)
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun content(form: NewEventForm): JsonObject {
        return Json.parseToJsonElement(form.toMeetingCreateContent("a-uid", berlin, Locale.GERMANY)).jsonObject
    }

    private val JsonObject.data get() = getValue("data").jsonObject
    private val JsonObject.calendar get() = data.getValue("calendar").jsonArray.single().jsonObject

    @Test
    fun `the event type is the one of the meetings bot`() {
        assertThat(MEETING_CREATE_EVENT_TYPE).isEqualTo("net.nordeck.meetings.meeting.create")
    }

    @Test
    fun `a single event`() {
        val json = content(aNewEventForm(title = "Sprint planning", description = "Plan it", start = monday.atTime(14, 30)))
        assertThat(json.data.getValue("title").jsonPrimitive.content).isEqualTo("Sprint planning")
        assertThat(json.data.getValue("description").jsonPrimitive.content).isEqualTo("Plan it")
        val entry = json.calendar
        assertThat(entry.getValue("uid").jsonPrimitive.content).isEqualTo("a-uid")
        assertThat(entry.getValue("dtstart").jsonObject.getValue("tzid").jsonPrimitive.content).isEqualTo("Europe/Berlin")
        assertThat(entry.getValue("dtstart").jsonObject.getValue("value").jsonPrimitive.content).isEqualTo("20260921T143000")
        assertThat(entry.getValue("dtend").jsonObject.getValue("tzid").jsonPrimitive.content).isEqualTo("Europe/Berlin")
        assertThat(entry.getValue("dtend").jsonObject.getValue("value").jsonPrimitive.content).isEqualTo("20260921T153000")
    }

    @Test
    fun `the values match the format required by the bot`() {
        val entry = content(aNewEventForm(title = "a")).calendar
        val format = Regex("^[\\d]{8}T[\\d]{6}$")
        assertThat(entry.getValue("dtstart").jsonObject.getValue("value").jsonPrimitive.content).matches(format.toPattern())
        assertThat(entry.getValue("dtend").jsonObject.getValue("value").jsonPrimitive.content).matches(format.toPattern())
    }

    @Test
    fun `the description is always sent because the bot requires it`() {
        assertThat(content(aNewEventForm(title = "a")).data.getValue("description").jsonPrimitive.content).isEmpty()
    }

    @Test
    fun `the title and the description are trimmed`() {
        val json = content(aNewEventForm(title = "  Title  ", description = " Text\n"))
        assertThat(json.data.getValue("title").jsonPrimitive.content).isEqualTo("Title")
        assertThat(json.data.getValue("description").jsonPrimitive.content).isEqualTo("Text")
    }

    @Test
    fun `the context has the language and the time zone`() {
        val context = content(aNewEventForm(title = "a")).getValue("context").jsonObject
        assertThat(context.getValue("locale").jsonPrimitive.content).isEqualTo("de-DE")
        assertThat(context.getValue("timezone").jsonPrimitive.content).isEqualTo("Europe/Berlin")
    }

    @Test
    fun `a repeating event has a recurrence rule`() {
        val entry = content(aNewEventForm(title = "a", recurrence = EventRecurrence.Weekly)).calendar
        assertThat(entry.getValue("rrule").jsonPrimitive.content).isEqualTo("FREQ=WEEKLY;BYDAY=MO")
    }

    @Test
    fun `an event that does not repeat has no recurrence rule`() {
        assertThat(content(aNewEventForm(title = "a")).calendar.containsKey("rrule")).isFalse()
    }

    @Test
    fun `an all day event lasts from midnight to the midnight ending its last day`() {
        val form = aNewEventForm(
            title = "a",
            isAllDay = true,
            start = monday.atTime(15, 0),
            end = monday.plusDays(1).atTime(8, 0),
        )
        val entry = content(form).calendar
        assertThat(entry.getValue("dtstart").jsonObject.getValue("value").jsonPrimitive.content).isEqualTo("20260921T000000")
        assertThat(entry.getValue("dtend").jsonObject.getValue("value").jsonPrimitive.content).isEqualTo("20260923T000000")
    }

    @Test
    fun `the participants are listed with their user id`() {
        val form = aNewEventForm(title = "a")
            .withParticipant(aMatrixUser(id = "@alice:server.org"))
            .withParticipant(aMatrixUser(id = "@bob:server.org"))
        val participants = content(form).data.getValue("participants").jsonArray
        val userIds = participants.map { it.jsonObject.getValue("user_id").jsonPrimitive.content }
        assertThat(userIds).containsExactly("@alice:server.org", "@bob:server.org").inOrder()
    }

    @Test
    fun `there is no participants list without participants`() {
        assertThat(content(aNewEventForm(title = "a")).data.containsKey("participants")).isFalse()
    }

    @Test
    fun `the message is not mixing the calendar with the deprecated start and end times`() {
        // The bot refuses a message having both, see IsIncompatibleWithSibling in MeetingCreateDto.
        val data = content(aNewEventForm(title = "a")).data
        assertThat(data.containsKey("start_time")).isFalse()
        assertThat(data.containsKey("end_time")).isFalse()
    }
}
