/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import androidx.annotation.StringRes
import cloud.orelit.communicationplatform.features.home.impl.R
import java.time.LocalDate

/** How often an event repeats. */
enum class EventRecurrence(
    @StringRes
    val labelRes: Int,
) {
    None(R.string.screen_calendar_repeat_none),
    Daily(R.string.screen_calendar_repeat_daily),
    Weekdays(R.string.screen_calendar_repeat_weekdays),
    Weekly(R.string.screen_calendar_repeat_weekly),
    Monthly(R.string.screen_calendar_repeat_monthly),
    Yearly(R.string.screen_calendar_repeat_yearly);

    /**
     * The iCalendar recurrence rule of a series starting on [start], or null if the event does not repeat.
     * The meetings bot expects the RFC 5545 syntax, see ADR003 of matrix-meetings.
     */
    fun toRRule(start: LocalDate): String? = when (this) {
        None -> null
        Daily -> "FREQ=DAILY"
        Weekdays -> "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
        // MONDAY -> MO, TUESDAY -> TU, ...
        Weekly -> "FREQ=WEEKLY;BYDAY=${start.dayOfWeek.name.take(2)}"
        Monthly -> "FREQ=MONTHLY;BYMONTHDAY=${start.dayOfMonth}"
        Yearly -> "FREQ=YEARLY"
    }

    companion object {
        /**
         * The option a stored rule was written from, the reverse of [toRRule], so an event being edited shows how it
         * repeats. A rule this app does not write falls back to the closest frequency, or [None].
         */
        fun fromRRule(rrule: String?): EventRecurrence {
            if (rrule.isNullOrBlank()) return None
            val parts = rrule.split(';').associate { part ->
                part.substringBefore('=', missingDelimiterValue = "").uppercase() to part.substringAfter('=', missingDelimiterValue = "").uppercase()
            }
            return when (parts["FREQ"]) {
                "DAILY" -> Daily
                "WEEKLY" -> if (parts["BYDAY"] == "MO,TU,WE,TH,FR") Weekdays else Weekly
                "MONTHLY" -> Monthly
                "YEARLY" -> Yearly
                else -> None
            }
        }
    }
}
