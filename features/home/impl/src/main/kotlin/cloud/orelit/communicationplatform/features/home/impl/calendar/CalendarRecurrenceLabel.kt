/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.annotation.StringRes
import cloud.orelit.communicationplatform.features.home.impl.R

/**
 * How to describe a recurrence rule to the user, the reverse of `EventRecurrence.toRRule`.
 *
 * A rule this app did not write, for instance one created by another client, has no label of its own, so it falls
 * back to saying only that the event repeats rather than describing it wrongly.
 */
@StringRes
internal fun recurrenceLabelRes(rrule: String): Int {
    val parts = rrule.split(';').associate { part ->
        part.substringBefore('=', missingDelimiterValue = "").uppercase() to part.substringAfter('=', missingDelimiterValue = "").uppercase()
    }
    return when (parts["FREQ"]) {
        "DAILY" -> R.string.screen_calendar_repeat_daily
        "WEEKLY" -> if (parts["BYDAY"] == WEEKDAYS) R.string.screen_calendar_repeat_weekdays else R.string.screen_calendar_repeat_weekly
        "MONTHLY" -> R.string.screen_calendar_repeat_monthly
        "YEARLY" -> R.string.screen_calendar_repeat_yearly
        else -> R.string.screen_calendar_repeat_other
    }
}

private const val WEEKDAYS = "MO,TU,WE,TH,FR"
