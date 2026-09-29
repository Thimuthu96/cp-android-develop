/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class EventRecurrenceTest {
    // A Monday, the 21st
    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun `an event that does not repeat has no rule`() {
        assertThat(EventRecurrence.None.toRRule(monday)).isNull()
    }

    @Test
    fun `fixed rules`() {
        assertThat(EventRecurrence.Daily.toRRule(monday)).isEqualTo("FREQ=DAILY")
        assertThat(EventRecurrence.Weekdays.toRRule(monday)).isEqualTo("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR")
        assertThat(EventRecurrence.Yearly.toRRule(monday)).isEqualTo("FREQ=YEARLY")
    }

    @Test
    fun `the weekly rule uses the day of the week of the start`() {
        val expected = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
        expected.forEachIndexed { offset, day ->
            assertThat(EventRecurrence.Weekly.toRRule(monday.plusDays(offset.toLong()))).isEqualTo("FREQ=WEEKLY;BYDAY=$day")
        }
    }

    @Test
    fun `the monthly rule uses the day of the month of the start`() {
        assertThat(EventRecurrence.Monthly.toRRule(monday)).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=21")
        assertThat(EventRecurrence.Monthly.toRRule(LocalDate.of(2026, 1, 5))).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=5")
    }

    @Test
    fun `every option is read back from the rule it writes`() {
        (0L..6L).map { monday.plusDays(it) }.forEach { start ->
            EventRecurrence.entries.forEach { recurrence ->
                assertThat(EventRecurrence.fromRRule(recurrence.toRRule(start))).isEqualTo(recurrence)
            }
        }
    }

    @Test
    fun `no rule reads as not repeating`() {
        assertThat(EventRecurrence.fromRRule(null)).isEqualTo(EventRecurrence.None)
        assertThat(EventRecurrence.fromRRule("")).isEqualTo(EventRecurrence.None)
        assertThat(EventRecurrence.fromRRule("   ")).isEqualTo(EventRecurrence.None)
    }
}
