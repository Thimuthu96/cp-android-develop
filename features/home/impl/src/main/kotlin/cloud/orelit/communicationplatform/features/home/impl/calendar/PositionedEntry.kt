/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

/**
 * A timed entry positioned in the column of a single day.
 *
 * Minutes are counted from the start of the day, in `0..MINUTES_PER_DAY`.
 * The entry takes the [column]-th slot out of [columnCount] equally sized slots.
 */
internal data class PositionedEntry(
    val entry: CalendarEntry,
    val startMinute: Int,
    val endMinute: Int,
    val column: Int,
    val columnCount: Int,
)
