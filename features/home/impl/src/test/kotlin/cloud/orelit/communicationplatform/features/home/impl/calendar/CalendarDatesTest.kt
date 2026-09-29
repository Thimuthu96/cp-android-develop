/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarDatesTest {
    // A Monday
    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun `start of week depends on the first day of the week`() {
        val wednesday = monday.plusDays(2)
        assertThat(wednesday.startOfWeek(DayOfWeek.MONDAY)).isEqualTo(monday)
        assertThat(wednesday.startOfWeek(DayOfWeek.SUNDAY)).isEqualTo(monday.minusDays(1))
        assertThat(monday.startOfWeek(DayOfWeek.MONDAY)).isEqualTo(monday)
    }

    @Test
    fun `the initial page starts on the anchor`() {
        listOf(1, 3, 7).forEach { step ->
            assertThat(pageToStartDate(CALENDAR_INITIAL_PAGE, monday, step)).isEqualTo(monday)
            assertThat(dateToPage(monday, monday, step)).isEqualTo(CALENDAR_INITIAL_PAGE)
        }
    }

    @Test
    fun `swiping moves by the number of days of a page`() {
        assertThat(pageToStartDate(CALENDAR_INITIAL_PAGE + 1, monday, 1)).isEqualTo(monday.plusDays(1))
        assertThat(pageToStartDate(CALENDAR_INITIAL_PAGE + 1, monday, 3)).isEqualTo(monday.plusDays(3))
        assertThat(pageToStartDate(CALENDAR_INITIAL_PAGE - 1, monday, 7)).isEqualTo(monday.minusDays(7))
    }

    @Test
    fun `a date is displayed by the page containing it, also in the past`() {
        // 3 days pages: [0..2], [3..5], ... and [-3..-1], [-6..-4], ...
        assertThat(dateToPage(monday.plusDays(2), monday, 3)).isEqualTo(CALENDAR_INITIAL_PAGE)
        assertThat(dateToPage(monday.plusDays(3), monday, 3)).isEqualTo(CALENDAR_INITIAL_PAGE + 1)
        assertThat(dateToPage(monday.minusDays(1), monday, 3)).isEqualTo(CALENDAR_INITIAL_PAGE - 1)
        assertThat(dateToPage(monday.minusDays(3), monday, 3)).isEqualTo(CALENDAR_INITIAL_PAGE - 1)
        assertThat(dateToPage(monday.minusDays(4), monday, 3)).isEqualTo(CALENDAR_INITIAL_PAGE - 2)
    }

    @Test
    fun `page and date mapping round trips`() {
        listOf(1, 3, 7).forEach { step ->
            for (offset in -40L..40L) {
                val date = monday.plusDays(offset)
                val page = dateToPage(date, monday, step)
                val start = pageToStartDate(page, monday, step)
                assertThat(start <= date).isTrue()
                assertThat(date < start.plusDays(step.toLong())).isTrue()
            }
        }
    }

    @Test
    fun `a date far away is kept in the page range`() {
        assertThat(dateToPage(monday.plusYears(1000), monday, 1)).isEqualTo(CALENDAR_PAGE_COUNT - 1)
        assertThat(dateToPage(monday.minusYears(1000), monday, 1)).isEqualTo(0)
    }

    @Test
    fun `the now indicator is only displayed on today`() {
        val now = LocalDateTime.of(2026, 9, 21, 10, 24)
        assertThat(nowIndicatorMinute(now, monday)).isEqualTo(10 * 60 + 24)
        assertThat(nowIndicatorMinute(now, monday.plusDays(1))).isNull()
        assertThat(nowIndicatorMinute(now, monday.minusDays(1))).isNull()
    }
}
