/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The room message type the meetings bot listens to, see "Room Messages" in docs/data-model.md of matrix-meetings. */
internal const val MEETING_CREATE_EVENT_TYPE = "net.nordeck.meetings.meeting.create"

/** The format of the values of a calendar entry, e.g. `20220721T184500`, imposed by CalendarEntryDto of the bot. */
private val CalendarValueFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.ROOT)

/**
 * The content of the [MEETING_CREATE_EVENT_TYPE] message asking the bot to create a meeting for this form.
 *
 * The bot only accepts date-times, so like Outlook does an all day event lasts from midnight to the midnight ending its last day.
 *
 * @param uid the identifier of the calendar entry, random.
 * @param zoneId the time zone the times of the form are in.
 * @param locale the language the bot uses to write its messages to the user.
 */
internal fun NewEventForm.toMeetingCreateContent(
    uid: String,
    zoneId: ZoneId,
    locale: Locale,
): String {
    val calendarStart: LocalDateTime
    val calendarEnd: LocalDateTime
    if (isAllDay) {
        calendarStart = start.toLocalDate().atStartOfDay()
        calendarEnd = end.toLocalDate().plusDays(1).atStartOfDay()
    } else {
        calendarStart = start
        calendarEnd = end
    }
    return buildJsonObject {
        putJsonObject("data") {
            put("title", title.trim())
            // Required by the bot, even when empty.
            put("description", description.trim())
            putJsonArray("calendar") {
                add(
                    buildJsonObject {
                        put("uid", uid)
                        putJsonObject("dtstart") {
                            put("tzid", zoneId.id)
                            put("value", CalendarValueFormatter.format(calendarStart))
                        }
                        putJsonObject("dtend") {
                            put("tzid", zoneId.id)
                            put("value", CalendarValueFormatter.format(calendarEnd))
                        }
                        recurrence.toRRule(calendarStart.toLocalDate())?.let { put("rrule", it) }
                    }
                )
            }
            if (participants.isNotEmpty()) {
                put(
                    "participants",
                    buildJsonArray {
                        participants.forEach { participant ->
                            add(buildJsonObject { put("user_id", participant.userId.value) })
                        }
                    }
                )
            }
        }
        putJsonObject("context") {
            put("locale", locale.toLanguageTag())
            put("timezone", zoneId.id)
        }
    }.toString()
}
