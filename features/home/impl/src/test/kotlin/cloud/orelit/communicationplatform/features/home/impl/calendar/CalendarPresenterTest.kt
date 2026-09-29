/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import app.cash.turbine.ReceiveTurbine
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.features.call.api.ElementCallEntryPoint
import cloud.orelit.communicationplatform.features.call.test.FakeElementCallEntryPoint
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarEventRepository
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CreateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeCalendarEventRepository
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeCreateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeUpdateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.UpdateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsEvent
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserRepository
import cloud.orelit.communicationplatform.libraries.usersearch.test.FakeUserRepository
import cloud.orelit.communicationplatform.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

private val AN_ERROR = RuntimeException("The room could not be created")

class CalendarPresenterTest {
    @Test
    fun `present - initial state shows the day view on today`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            assertThat(state.viewMode).isEqualTo(CalendarViewMode.Day)
            assertThat(state.selectedDate).isEqualTo(state.today)
            assertThat(state.now.toLocalDate()).isEqualTo(state.today)
            assertThat(state.eventForm).isNull()
        }
    }

    @Test
    fun `present - select date`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            val date = initialState.today.plusDays(10)
            initialState.eventSink(CalendarEvent.SelectDate(date))
            assertThat(awaitItem().selectedDate).isEqualTo(date)
        }
    }

    @Test
    fun `present - select view mode`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            CalendarViewMode.entries.filter { it != initialState.viewMode }.forEach { mode ->
                initialState.eventSink(CalendarEvent.SelectViewMode(mode))
                assertThat(awaitItem().viewMode).isEqualTo(mode)
            }
        }
    }

    @Test
    fun `present - go to today after selecting another date`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            initialState.eventSink(CalendarEvent.SelectDate(LocalDate.of(2030, 1, 1)))
            val otherDayState = awaitItem()
            assertThat(otherDayState.selectedDate).isEqualTo(LocalDate.of(2030, 1, 1))
            otherDayState.eventSink(CalendarEvent.GoToToday)
            assertThat(awaitItem().selectedDate).isEqualTo(otherDayState.today)
        }
    }

    @Test
    fun `present - new event opens the form on the selected date`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            val date = initialState.today.plusDays(4)
            initialState.eventSink(CalendarEvent.SelectDate(date))
            awaitItem().eventSink(CalendarEvent.NewEvent)
            val form = awaitItem().eventForm!!.form
            assertThat(form.start.toLocalDate()).isEqualTo(date)
            assertThat(form.title).isEmpty()
            assertThat(form.canSave).isFalse()
        }
    }

    @Test
    fun `present - the form can be edited`() = runTest {
        createPresenter().test {
            val state = openForm()
            state.eventForm!!.eventSink(EventFormEvent.SetTitle("Sprint planning"))
            state.eventForm!!.eventSink(EventFormEvent.SetDescription("Plan it"))
            state.eventForm!!.eventSink(EventFormEvent.SetRecurrence(EventRecurrence.Weekly))
            state.eventForm!!.eventSink(EventFormEvent.SetAllDay(true))
            val form = awaitState { it.eventForm?.form?.isAllDay == true }.eventForm!!.form
            assertThat(form.title).isEqualTo("Sprint planning")
            assertThat(form.description).isEqualTo("Plan it")
            assertThat(form.recurrence).isEqualTo(EventRecurrence.Weekly)
            assertThat(form.canSave).isTrue()
        }
    }

    @Test
    fun `present - changing the start keeps the duration`() = runTest {
        createPresenter().test {
            val state = openForm()
            val form = state.eventForm!!.form
            val newStart = form.start.plusDays(2).withHour(15).withMinute(0)
            state.eventForm!!.eventSink(EventFormEvent.SetStart(newStart))
            val updated = awaitState { it.eventForm?.form?.start == newStart }.eventForm!!.form
            assertThat(updated.end).isEqualTo(newStart.plus(Duration.between(form.start, form.end)))
        }
    }

    @Test
    fun `present - an invalid form is not sent`() = runTest {
        val action = FakeCreateCalendarEventAction()
        createPresenter(createCalendarEventAction = action).test {
            val state = openForm()
            // No title
            state.eventForm!!.eventSink(EventFormEvent.Save)
            assertThat(action.forms).isEmpty()
            val past = LocalDateTime.of(2000, 1, 1, 0, 0)
            state.eventForm!!.eventSink(EventFormEvent.SetTitle("Title"))
            state.eventForm!!.eventSink(EventFormEvent.SetEnd(past))
            val invalid = awaitState { it.eventForm?.form?.end == past }
            invalid.eventForm!!.eventSink(EventFormEvent.Save)
            assertThat(action.forms).isEmpty()
        }
    }

    @Test
    fun `present - saving sends the event, closes the form and tells the user`() = runTest {
        val action = FakeCreateCalendarEventAction()
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(createCalendarEventAction = action, snackbarDispatcher = snackbarDispatcher).test {
            val state = openForm()
            state.eventForm!!.eventSink(EventFormEvent.SetTitle("Sprint planning"))
            awaitState { it.eventForm?.form?.title == "Sprint planning" }.eventForm!!.eventSink(EventFormEvent.Save)
            awaitState { it.eventForm == null }
            assertThat(action.forms.map { it.title }).containsExactly("Sprint planning")
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_sent)
        }
    }

    @Test
    fun `present - a failure to send keeps the form and can be dismissed`() = runTest {
        val action = FakeCreateCalendarEventAction(result = { Result.failure(AN_ERROR) })
        createPresenter(createCalendarEventAction = action).test {
            val state = openForm()
            state.eventForm!!.eventSink(EventFormEvent.SetTitle("Sprint planning"))
            awaitState { it.eventForm?.form?.title == "Sprint planning" }.eventForm!!.eventSink(EventFormEvent.Save)
            val failed = awaitState { it.eventForm?.saveAction is AsyncAction.Failure }
            assertThat(failed.eventForm!!.form.title).isEqualTo("Sprint planning")
            assertThat((failed.eventForm!!.saveAction as AsyncAction.Failure).error).isEqualTo(AN_ERROR)
            failed.eventForm!!.eventSink(EventFormEvent.ClearSaveError)
            assertThat(awaitState { it.eventForm?.saveAction == AsyncAction.Uninitialized }.eventForm).isNotNull()
        }
    }

    @Test
    fun `present - dismissing closes the form`() = runTest {
        createPresenter().test {
            val state = openForm()
            state.eventForm!!.eventSink(EventFormEvent.Dismiss)
            assertThat(awaitItem().eventForm).isNull()
        }
    }

    @Test
    fun `present - participants can be toggled and removed`() = runTest {
        createPresenter().test {
            val state = openForm()
            val alice = aMatrixUser(id = "@alice:server.org", displayName = "Alice")
            val bob = aMatrixUser(id = "@bob:server.org", displayName = "Bob")
            state.eventForm!!.eventSink(EventFormEvent.ToggleParticipant(alice))
            state.eventForm!!.eventSink(EventFormEvent.ToggleParticipant(bob))
            val both = awaitState { it.eventForm?.form?.participants?.size == 2 }
            both.eventForm!!.eventSink(EventFormEvent.ToggleParticipant(alice))
            val onlyBob = awaitState { it.eventForm?.form?.participants?.size == 1 }
            assertThat(onlyBob.eventForm!!.form.participants.single().userId).isEqualTo(bob.userId)
            onlyBob.eventForm!!.eventSink(EventFormEvent.RemoveParticipant(bob))
            assertThat(awaitState { it.eventForm?.form?.participants?.isEmpty() == true }.eventForm).isNotNull()
        }
    }

    @Test
    fun `present - the participant picker can be opened and closed`() = runTest {
        createPresenter().test {
            val state = openForm()
            assertThat(state.eventForm!!.participantPicker).isNull()
            state.eventForm!!.eventSink(EventFormEvent.OpenParticipantPicker)
            val picking = awaitState { it.eventForm?.participantPicker != null }
            assertThat(picking.eventForm!!.participantPicker!!.results).isEmpty()
            picking.eventForm!!.eventSink(EventFormEvent.CloseParticipantPicker)
            assertThat(awaitState { it.eventForm?.participantPicker == null }.eventForm).isNotNull()
        }
    }

    @Test
    fun `present - the entries come from the repository`() = runTest {
        val entry = aCalendarEntry(id = "standup", title = "Daily stand-up")
        val repository = FakeCalendarEventRepository(persistentListOf(entry))
        createPresenter(calendarEventRepository = repository).test {
            val state = awaitState { it.entries.isNotEmpty() }
            assertThat(state.entries).containsExactly(entry)
        }
    }

    @Test
    fun `present - the repository is asked for a window around today`() = runTest {
        val repository = FakeCalendarEventRepository()
        createPresenter(calendarEventRepository = repository).test {
            val state = awaitItem()
            val request = repository.lastRequest!!
            assertThat(request.around).isEqualTo(state.today)
            assertThat(request.daysBefore).isGreaterThan(0L)
            assertThat(request.daysAfter).isGreaterThan(request.daysBefore)
        }
    }

    @Test
    fun `present - tapping a grid slot opens the form at that time`() = runTest {
        createPresenter().test {
            val initialState = awaitItem()
            val start = initialState.today.plusDays(3).atTime(14, 30)
            initialState.eventSink(CalendarEvent.NewEventAt(start))
            val form = awaitState { it.eventForm != null }.eventForm!!.form
            assertThat(form.start).isEqualTo(start)
            // The default duration is kept, so the slot only decides when the event begins.
            assertThat(form.end).isEqualTo(start.plus(NewEventForm.DEFAULT_DURATION))
        }
    }

    @Test
    fun `present - tapping an entry opens its details`() = runTest {
        val entry = aCalendarEntry(id = "standup", title = "Daily stand-up", room = aCalendarEntryRoom())
        val repository = FakeCalendarEventRepository(persistentListOf(entry))
        createPresenter(calendarEventRepository = repository).test {
            val state = awaitState { it.entries.isNotEmpty() }
            state.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            assertThat(details.entry).isEqualTo(entry)
            assertThat(details.participantCount).isEqualTo(entry.room!!.participantCount)
            assertThat(details.hasRoom).isTrue()
        }
    }

    @Test
    fun `present - the details can be dismissed`() = runTest {
        val entry = aCalendarEntry(id = "standup", room = aCalendarEntryRoom())
        createPresenter(calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            details.eventSink(CalendarEventDetailsEvent.Dismiss)
            assertThat(awaitState { it.eventDetails == null }.eventDetails).isNull()
        }
    }

    @Test
    fun `present - joining the call starts it in the room of the event and closes the details`() = runTest {
        val roomId = RoomId("!event:example.org")
        val entry = aCalendarEntry(id = "standup", room = aCalendarEntryRoom(roomId = roomId))
        val startedCalls = mutableListOf<CallData>()
        val callEntryPoint = FakeElementCallEntryPoint(startCallResult = { startedCalls += it })
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            elementCallEntryPoint = callEntryPoint,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.JoinCall)
            awaitState { it.eventDetails == null }
            assertThat(startedCalls.map { it.roomId }).containsExactly(roomId)
            assertThat(startedCalls.single().isAudioCall).isFalse()
        }
    }

    @Test
    fun `present - the meeting link is resolved while the details are open`() = runTest {
        val roomId = RoomId("!event:example.org")
        val permalink = "https://matrix.to/#/!event:example.org"
        val entry = aCalendarEntry(id = "standup", room = aCalendarEntryRoom(roomId = roomId))
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(
                roomId = roomId,
                result = FakeJoinedRoom(baseRoom = FakeBaseRoom(roomPermalinkResult = { Result.success(permalink) })),
            )
        }
        createPresenter(client = client, calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails?.meetingLink != null }.eventDetails!!
            assertThat(details.meetingLink).isEqualTo(permalink)
        }
    }

    @Test
    fun `present - copying the link confirms it to the user`() = runTest {
        val entry = aCalendarEntry(id = "standup", room = aCalendarEntryRoom())
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.CopyLink)
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId)
                .isEqualTo(R.string.screen_calendar_event_link_copied)
        }
    }

    @Test
    fun `present - an event without a room cannot be joined`() = runTest {
        val entry = aCalendarEntry(id = "sample", room = null)
        val snackbarDispatcher = SnackbarDispatcher()
        val callEntryPoint = FakeElementCallEntryPoint(startCallResult = { error("No call should be started") })
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            elementCallEntryPoint = callEntryPoint,
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            assertThat(details.hasRoom).isFalse()
            assertThat(details.meetingLink).isNull()
            details.eventSink(CalendarEventDetailsEvent.JoinCall)
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId)
                .isEqualTo(R.string.screen_calendar_event_error_no_room)
        }
    }

    @Test
    fun `present - opening the form closes the details`() = runTest {
        val entry = aCalendarEntry(id = "standup", room = aCalendarEntryRoom())
        createPresenter(calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventSink(CalendarEvent.NewEvent)
            val state = awaitState { it.eventForm != null }
            assertThat(state.eventDetails).isNull()
        }
    }

    /** Opens the form and returns the state having it. */
    private suspend fun ReceiveTurbine<CalendarState>.openForm(): CalendarState {
        awaitItem().eventSink(CalendarEvent.NewEvent)
        return awaitState { it.eventForm != null }
    }

    /** Skips the states until one matches, the presenter may emit intermediate states. */
    private suspend fun ReceiveTurbine<CalendarState>.awaitState(predicate: (CalendarState) -> Boolean): CalendarState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }

    private fun createPresenter(
        client: MatrixClient = FakeMatrixClient(),
        userRepository: UserRepository = FakeUserRepository(),
        calendarEventRepository: CalendarEventRepository = FakeCalendarEventRepository(),
        createCalendarEventAction: CreateCalendarEventAction = FakeCreateCalendarEventAction(),
        updateCalendarEventAction: UpdateCalendarEventAction = FakeUpdateCalendarEventAction(),
        elementCallEntryPoint: ElementCallEntryPoint = FakeElementCallEntryPoint(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    ) = CalendarPresenter(
        client = client,
        userRepository = userRepository,
        calendarEventRepository = calendarEventRepository,
        createCalendarEventAction = createCalendarEventAction,
        updateCalendarEventAction = updateCalendarEventAction,
        elementCallEntryPoint = elementCallEntryPoint,
        snackbarDispatcher = snackbarDispatcher,
    )
}
