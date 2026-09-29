/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.annotation.StringRes
import cloud.orelit.communicationplatform.features.home.impl.R

enum class CalendarViewMode(
    /** Number of days shown side by side. Only meaningful for time grid modes. */
    val days: Int,
    @StringRes
    val labelRes: Int,
) {
    Agenda(days = 1, labelRes = R.string.screen_calendar_view_agenda),
    Day(days = 1, labelRes = R.string.screen_calendar_view_day),
    ThreeDay(days = 3, labelRes = R.string.screen_calendar_view_three_day),
    Week(days = 7, labelRes = R.string.screen_calendar_view_week);

    val isTimeGrid: Boolean get() = this != Agenda

    companion object {
        fun from(ordinal: Int): CalendarViewMode = entries.getOrElse(ordinal) { Day }
    }
}
