/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Reads and writes the schedule of an event in the topic of its room.
 *
 * The homeserver stores no calendar of its own, so an event is a normal Matrix room whose topic carries both the
 * description the user typed and the machine-readable schedule, separated by [SEPARATOR]. That character is an
 * invisible separator, so a client that knows nothing about events still shows a sensible topic.
 *
 * The payload is versioned: a topic written by a newer version than [VERSION] is ignored rather than
 * misinterpreted, which leaves the room out of the calendar instead of showing it at the wrong time.
 */
internal object CalendarRoomEncoding {
    /** U+2063 INVISIBLE SEPARATOR, which renders as nothing and is not something a user types. */
    const val SEPARATOR = '⁣'

    /**
     * The newest schema version this code reads.
     *
     * Version 2 adds the cancellation and the per-occurrence changes. It is only written when the schedule carries
     * any of them, see [CalendarSchedule.hasExceptions], so an older client drops a changed event instead of showing
     * it wrong, and still shows every event that was never changed.
     */
    const val VERSION = 2

    /** The version written for a schedule without exceptions, which every client understands. */
    private const val BASE_VERSION = 1

    /** Local wall-clock times, e.g. `20260924T140000`, the same shape the meetings bot uses. */
    private val TimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.ROOT)

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * The topic to store for an event whose description is [description].
     * The description is kept first so it is what a user reads.
     */
    fun encode(description: String, schedule: CalendarSchedule): String {
        val payload = buildJsonObject {
            put("v", if (schedule.hasExceptions) VERSION else BASE_VERSION)
            put("start", TimeFormatter.format(schedule.start))
            put("end", TimeFormatter.format(schedule.end))
            put("tz", schedule.timeZone.id)
            put("allDay", schedule.isAllDay)
            schedule.rrule?.let { put("rrule", it) }
            if (schedule.isCancelled) put("cancelled", true)
            if (schedule.cancelledOccurrences.isNotEmpty()) {
                putJsonArray("cancelledOn") {
                    schedule.cancelledOccurrences.sorted().forEach { add(TimeFormatter.format(it)) }
                }
            }
            if (schedule.overrides.isNotEmpty()) {
                putJsonArray("overrides") {
                    schedule.overrides.entries.sortedBy { it.key }.forEach { (original, override) ->
                        addJsonObject {
                            put("orig", TimeFormatter.format(original))
                            put("start", TimeFormatter.format(override.start))
                            put("end", TimeFormatter.format(override.end))
                            override.title?.let { put("title", it) }
                            override.description?.let { put("description", it) }
                        }
                    }
                }
            }
        }
        // A separator pasted into the description would make the payload start at the wrong place.
        return description.replace(SEPARATOR.toString(), "").trim() + SEPARATOR + payload.toString()
    }

    /** The description a topic carries, without the payload, or null when it is empty. */
    fun decodeDescription(topic: String?): String? {
        return topic?.substringBefore(SEPARATOR)?.trim()?.takeIf { it.isNotEmpty() }
    }

    /**
     * The schedule a topic carries, or null when there is none, when it is malformed, or when it was written by a
     * newer version of the schema. A room without a schedule is simply not an event room.
     */
    fun decodeSchedule(topic: String?): CalendarSchedule? {
        if (topic == null || SEPARATOR !in topic) return null
        val payload = topic.substringAfter(SEPARATOR).trim()
        if (payload.isEmpty()) return null
        return runCatching {
            val obj = json.parseToJsonElement(payload) as? JsonObject ?: return null
            val version = obj["v"]?.jsonPrimitive?.int ?: return null
            if (version > VERSION) {
                Timber.d("Ignoring a calendar room written with schema version $version")
                return null
            }
            val start = obj.string("start")?.toLocalDateTime() ?: return null
            val end = obj.string("end")?.toLocalDateTime() ?: return null
            // An event that ends before it starts is corrupt; dropping it is better than laying out a negative height.
            if (end.isBefore(start)) return null
            CalendarSchedule(
                start = start,
                end = end,
                timeZone = obj.string("tz")?.toZoneId() ?: ZoneId.systemDefault(),
                isAllDay = (obj["allDay"] as? JsonPrimitive)?.booleanOrNull() ?: false,
                rrule = obj.string("rrule"),
                isCancelled = (obj["cancelled"] as? JsonPrimitive)?.booleanOrNull() ?: false,
                cancelledOccurrences = (obj["cancelledOn"] as? JsonArray).orEmpty()
                    .mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.toLocalDateTime() }
                    .toSet(),
                overrides = (obj["overrides"] as? JsonArray).orEmpty()
                    .mapNotNull { (it as? JsonObject)?.toOverride() }
                    .toMap(),
            )
        }.getOrElse {
            Timber.d(it, "Could not read the schedule of a calendar room")
            null
        }
    }

    /** A malformed override is skipped on its own, so it cannot hide the rest of the series. */
    private fun JsonObject.toOverride(): Pair<LocalDateTime, OccurrenceOverride>? {
        // Safe casts rather than jsonPrimitive, which throws on an unexpected shape.
        fun primitive(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
        val original = primitive("orig")?.toLocalDateTime() ?: return null
        val start = primitive("start")?.toLocalDateTime() ?: return null
        val end = primitive("end")?.toLocalDateTime() ?: return null
        if (end.isBefore(start)) return null
        return original to OccurrenceOverride(
            start = start,
            end = end,
            title = primitive("title")?.takeIf { it.isNotEmpty() },
            description = primitive("description"),
        )
    }

    private fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotEmpty() }

    private fun JsonPrimitive.booleanOrNull(): Boolean? = runCatching { boolean }.getOrNull()

    private fun String.toLocalDateTime(): LocalDateTime? = try {
        LocalDateTime.parse(this, TimeFormatter)
    } catch (_: DateTimeParseException) {
        null
    }

    // An unknown zone id would throw, and the system default is a better guess than dropping the event.
    private fun String.toZoneId(): ZoneId? = runCatching { ZoneId.of(this) }.getOrNull()
}
