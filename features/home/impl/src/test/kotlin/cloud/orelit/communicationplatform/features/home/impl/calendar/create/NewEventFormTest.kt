/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class NewEventFormTest {
    private val monday = LocalDate.of(2026, 9, 21)

    @Test
    fun `an event created for today starts at the next half hour`() {
        assertThat(NewEventForm.initial(monday, monday.atTime(10, 10)).start).isEqualTo(monday.atTime(10, 30))
        assertThat(NewEventForm.initial(monday, monday.atTime(10, 30)).start).isEqualTo(monday.atTime(11, 0))
        assertThat(NewEventForm.initial(monday, monday.atTime(23, 45)).start).isEqualTo(monday.plusDays(1).atStartOfDay())
    }

    @Test
    fun `an event created for another day starts at 9`() {
        val form = NewEventForm.initial(monday.plusDays(3), monday.atTime(10, 10))
        assertThat(form.start).isEqualTo(monday.plusDays(3).atTime(9, 0))
        assertThat(form.end).isEqualTo(monday.plusDays(3).atTime(10, 0))
    }

    @Test
    fun `a new form has nothing to save`() {
        val form = NewEventForm.initial(monday, monday.atTime(10, 10))
        assertThat(form.canSave).isFalse()
        assertThat(form.isDirty).isFalse()
        assertThat(form.isTimeRangeValid).isTrue()
        assertThat(form.recurrence).isEqualTo(EventRecurrence.None)
    }

    @Test
    fun `a form with a title can be saved`() {
        assertThat(aNewEventForm(title = "Meeting").canSave).isTrue()
        assertThat(aNewEventForm(title = "   ").canSave).isFalse()
    }

    @Test
    fun `an event must end after it starts`() {
        val start = monday.atTime(10, 0)
        assertThat(aNewEventForm(title = "a", start = start, end = start.plusMinutes(1)).canSave).isTrue()
        assertThat(aNewEventForm(title = "a", start = start, end = start).canSave).isFalse()
        assertThat(aNewEventForm(title = "a", start = start, end = start.minusMinutes(1)).canSave).isFalse()
    }

    @Test
    fun `an all day event may start and end on the same day, whatever the times`() {
        val start = monday.atTime(10, 0)
        val sameDayEarlierTime = aNewEventForm(title = "a", isAllDay = true, start = start, end = monday.atTime(8, 0))
        assertThat(sameDayEarlierTime.isTimeRangeValid).isTrue()
        val previousDay = aNewEventForm(title = "a", isAllDay = true, start = start, end = monday.minusDays(1).atTime(12, 0))
        assertThat(previousDay.isTimeRangeValid).isFalse()
    }

    @Test
    fun `moving the start keeps the duration`() {
        val form = aNewEventForm(start = monday.atTime(10, 0), end = monday.atTime(11, 30))
            .withStart(monday.plusDays(2).atTime(15, 0))
        assertThat(form.start).isEqualTo(monday.plusDays(2).atTime(15, 0))
        assertThat(form.end).isEqualTo(monday.plusDays(2).atTime(16, 30))
    }

    @Test
    fun `moving the start of an invalid range restores the default duration`() {
        val form = aNewEventForm(start = monday.atTime(10, 0), end = monday.atTime(9, 0)).withStart(monday.atTime(12, 0))
        assertThat(Duration.between(form.start, form.end)).isEqualTo(NewEventForm.DEFAULT_DURATION)
    }

    @Test
    fun `moving the end does not move the start`() {
        val form = aNewEventForm(start = monday.atTime(10, 0), end = monday.atTime(11, 0)).withEnd(monday.atTime(9, 0))
        assertThat(form.start).isEqualTo(monday.atTime(10, 0))
        assertThat(form.end).isEqualTo(monday.atTime(9, 0))
    }

    @Test
    fun `participants are added once and can be removed`() {
        val alice = aMatrixUser(id = "@alice:server.org", displayName = "Alice")
        val bob = aMatrixUser(id = "@bob:server.org", displayName = "Bob")
        val form = aNewEventForm().withParticipant(alice).withParticipant(alice).withParticipant(bob)
        assertThat(form.participants.map { it.userId }).containsExactly(alice.userId, bob.userId).inOrder()
        assertThat(form.withoutParticipant(alice).participants.map { it.userId }).containsExactly(bob.userId)
    }

    @Test
    fun `the form is dirty once the user typed or picked something`() {
        assertThat(aNewEventForm(title = "a").isDirty).isTrue()
        assertThat(aNewEventForm(description = "a").isDirty).isTrue()
        assertThat(aNewEventForm().withParticipant(aMatrixUser()).isDirty).isTrue()
        assertThat(aNewEventForm(title = " ").isDirty).isFalse()
    }

    @Test
    fun `the times do not matter for the initial form of a past date`() {
        val past = LocalDateTime.of(2020, 1, 1, 12, 0)
        assertThat(NewEventForm.initial(past.toLocalDate().plusDays(1), past).start.toLocalTime().hour).isEqualTo(9)
    }
}
