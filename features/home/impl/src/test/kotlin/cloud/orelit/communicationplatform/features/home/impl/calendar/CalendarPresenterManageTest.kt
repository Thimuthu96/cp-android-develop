/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import app.cash.turbine.ReceiveTurbine
import cloud.orelit.communicationplatform.features.call.api.ElementCallEntryPoint
import cloud.orelit.communicationplatform.features.call.test.FakeElementCallEntryPoint
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormMode
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarEventRepository
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarRoomEncoding
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarSchedule
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CreateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeCalendarEventRepository
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeCreateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.FakeUpdateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.UpdateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.EventScope
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.ManageAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembersState
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomMember
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserRepository
import cloud.orelit.communicationplatform.libraries.usersearch.test.FakeUserRepository
import cloud.orelit.communicationplatform.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

private val AN_ERROR = RuntimeException("The room could not be created")
private val AN_EVENT_ROOM_ID = RoomId("!event:example.org")

/** The edit and cancel flow of the event details, kept apart from [CalendarPresenterTest] for its size. */
class CalendarPresenterManageTest {
    @Test
    fun `present - only the organiser can manage an event`() = runTest {
        val someoneElses = aCalendarEntry(id = "other", room = aCalendarEntryRoom(organizer = UserId("@bob:example.org")))
        val mine = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(organizer = A_SESSION_ID))
        val action = FakeUpdateCalendarEventAction()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(someoneElses, mine)),
            updateCalendarEventAction = action,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(someoneElses))
            val details = awaitState { it.eventDetails?.entry == someoneElses }.eventDetails!!
            assertThat(details.canManage).isFalse()
            assertThat(details.showManageActions).isFalse()
            // Even if asked, nothing happens for someone else's event.
            details.eventSink(CalendarEventDetailsEvent.Cancel)
            details.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            assertThat(action.cancellations).isEmpty()
            details.eventSink(CalendarEventDetailsEvent.Dismiss)

            awaitState { it.eventDetails == null }.eventSink(CalendarEvent.OpenEntry(mine))
            val myDetails = awaitState { it.eventDetails?.entry == mine }.eventDetails!!
            assertThat(myDetails.canManage).isTrue()
            assertThat(myDetails.showManageActions).isTrue()
        }
    }

    @Test
    fun `present - a cancelled event cannot be managed or joined any more`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(organizer = A_SESSION_ID), isCancelled = true)
        createPresenter(calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            assertThat(details.canManage).isTrue()
            assertThat(details.showManageActions).isFalse()
            assertThat(details.canJoinCall).isFalse()
            assertThat(details.hasRoom).isTrue()
        }
    }

    @Test
    fun `present - editing a one-off event prefills the form from its room`() = runTest {
        val entry = aCalendarEntry(id = "mine", title = "Design review", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            val state = awaitState { it.eventForm != null }
            val eventForm = state.eventForm!!
            assertThat(eventForm.mode).isEqualTo(EventFormMode.EditSeries(AN_EVENT_ROOM_ID))
            assertThat(eventForm.isEditing).isTrue()
            assertThat(eventForm.isDirty).isFalse()
            assertThat(eventForm.form.title).isEqualTo("Design review")
            assertThat(eventForm.form.description).isEqualTo("Plan the sprint")
            assertThat(eventForm.form.start).isEqualTo(entry.start)
            assertThat(eventForm.form.end).isEqualTo(entry.end)
            // Everyone in the room but the organiser.
            assertThat(eventForm.form.participants.map { it.userId }).containsExactly(UserId("@bob:example.org"))
            // The sheet waits behind the form.
            assertThat(state.eventDetails).isNull()
        }
    }

    @Test
    fun `present - saving an edit updates the event and tells the user`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        val action = FakeUpdateCalendarEventAction()
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            awaitState { it.eventForm != null }.eventForm!!.eventSink(EventFormEvent.SetTitle("Renamed"))
            val renamed = awaitState { it.eventForm?.form?.title == "Renamed" }
            assertThat(renamed.eventForm!!.isDirty).isTrue()
            renamed.eventForm!!.eventSink(EventFormEvent.Save)
            // Back on the sheet of the same event.
            val closed = awaitState { it.eventForm == null && it.eventDetails != null }
            assertThat(closed.eventDetails!!.entry.id).isEqualTo("mine")
            assertThat(action.seriesEdits.map { it.roomId to it.form.title }).containsExactly(AN_EVENT_ROOM_ID to "Renamed")
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_updated)
        }
    }

    @Test
    fun `present - a failure to save an edit keeps the form open`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = FakeUpdateCalendarEventAction(result = { Result.failure(AN_ERROR) }),
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            awaitState { it.eventForm != null }.eventForm!!.eventSink(EventFormEvent.SetTitle("Renamed"))
            awaitState { it.eventForm?.form?.title == "Renamed" }.eventForm!!.eventSink(EventFormEvent.Save)
            val failed = awaitState { it.eventForm?.saveAction is AsyncAction.Failure }
            assertThat(failed.eventForm!!.isEditing).isTrue()
        }
    }

    @Test
    fun `present - editing a recurring event asks which occurrences first`() = runTest {
        val occurrenceStart = LocalDateTime.of(2026, 10, 1, 14, 0)
        val entry = aCalendarEntry(
            id = "mine",
            rrule = "FREQ=WEEKLY;BYDAY=TH",
            occurrenceStart = occurrenceStart,
            room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID),
        )
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            val asking = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            assertThat(asking.pendingAction).isEqualTo(ManageAction.AskScope(ManageAction.Kind.Edit))
            asking.eventSink(CalendarEventDetailsEvent.ChooseScope(EventScope.Occurrence))
            val eventForm = awaitState { it.eventForm != null }.eventForm!!
            assertThat(eventForm.mode).isEqualTo(EventFormMode.EditOccurrence(AN_EVENT_ROOM_ID, occurrenceStart))
            assertThat(eventForm.isEditingOccurrence).isTrue()
            assertThat(eventForm.form.recurrence).isEqualTo(EventRecurrence.Weekly)
            // The repeat rule cannot be changed for a single occurrence.
            eventForm.eventSink(EventFormEvent.SetRecurrence(EventRecurrence.Daily))
            eventForm.eventSink(EventFormEvent.SetTitle("Only this one"))
            val edited = awaitState { it.eventForm?.form?.title == "Only this one" }
            assertThat(edited.eventForm!!.form.recurrence).isEqualTo(EventRecurrence.Weekly)
        }
    }

    @Test
    fun `present - the scope dialog can be dismissed`() = runTest {
        val entry = aCalendarEntry(
            id = "mine",
            rrule = "FREQ=DAILY",
            occurrenceStart = LocalDateTime.of(2026, 10, 1, 14, 0),
            room = aCalendarEntryRoom(organizer = A_SESSION_ID),
        )
        createPresenter(calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            val asking = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            assertThat(asking.pendingAction).isEqualTo(ManageAction.AskScope(ManageAction.Kind.Cancel))
            asking.eventSink(CalendarEventDetailsEvent.DismissDialog)
            assertThat(awaitState { it.eventDetails?.pendingAction == null }.eventDetails).isNotNull()
        }
    }

    @Test
    fun `present - cancelling a one-off event is confirmed first`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        val action = FakeUpdateCalendarEventAction()
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            val confirming = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            assertThat(confirming.pendingAction).isEqualTo(ManageAction.ConfirmCancel(EventScope.Series))
            assertThat(action.cancellations).isEmpty()
            confirming.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            awaitState { it.eventDetails?.pendingAction == null }
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_canceled)
            assertThat(action.cancellations).containsExactly(FakeUpdateCalendarEventAction.Cancellation(AN_EVENT_ROOM_ID, null))
        }
    }

    @Test
    fun `present - cancelling one occurrence of a recurring event`() = runTest {
        val occurrenceStart = LocalDateTime.of(2026, 10, 1, 14, 0)
        val entry = aCalendarEntry(
            id = "mine",
            rrule = "FREQ=WEEKLY;BYDAY=TH",
            occurrenceStart = occurrenceStart,
            room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID),
        )
        val action = FakeUpdateCalendarEventAction()
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            awaitState { it.eventDetails?.pendingAction is ManageAction.AskScope }.eventDetails!!
                .eventSink(CalendarEventDetailsEvent.ChooseScope(EventScope.Occurrence))
            val confirming = awaitState { it.eventDetails?.pendingAction is ManageAction.ConfirmCancel }.eventDetails!!
            assertThat(confirming.pendingAction).isEqualTo(ManageAction.ConfirmCancel(EventScope.Occurrence))
            confirming.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            awaitState { it.eventDetails?.pendingAction == null }
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_canceled)
            assertThat(action.cancellations).containsExactly(FakeUpdateCalendarEventAction.Cancellation(AN_EVENT_ROOM_ID, occurrenceStart))
        }
    }

    @Test
    fun `present - a failure to cancel is reported`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(organizer = A_SESSION_ID))
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = FakeUpdateCalendarEventAction(result = { Result.failure(AN_ERROR) }),
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            awaitState { it.eventDetails?.pendingAction == null }
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_cancel_failed)
        }
    }

    @Test
    fun `present - an occurrence is prefilled from its entry and saved on its own`() = runTest {
        val occurrenceStart = LocalDateTime.of(2026, 10, 1, 14, 0)
        val entry = aCalendarEntry(
            id = "mine",
            title = "Moved review",
            start = LocalDateTime.of(2026, 10, 2, 16, 0),
            end = LocalDateTime.of(2026, 10, 2, 17, 0),
            rrule = "FREQ=WEEKLY;BYDAY=TH",
            occurrenceStart = occurrenceStart,
            room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID),
        )
        val action = FakeUpdateCalendarEventAction()
        createPresenter(
            client = aClientWithEventRoom(entry, seriesStart = LocalDateTime.of(2026, 9, 24, 14, 0), seriesEnd = LocalDateTime.of(2026, 9, 24, 15, 0)),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.ChooseScope(EventScope.Occurrence))
            val eventForm = awaitState { it.eventForm != null }.eventForm!!
            // The occurrence as it is now, not the start of the series.
            assertThat(eventForm.form.start).isEqualTo(entry.start)
            assertThat(eventForm.form.end).isEqualTo(entry.end)
            assertThat(eventForm.form.title).isEqualTo("Moved review")
            assertThat(eventForm.canSave).isFalse()
            eventForm.eventSink(EventFormEvent.SetTitle("Only this one"))
            awaitState { it.eventForm?.form?.title == "Only this one" }.eventForm!!.eventSink(EventFormEvent.Save)
            awaitState { it.eventForm == null }
            assertThat(action.seriesEdits).isEmpty()
            assertThat(action.occurrenceEdits).hasSize(1)
            val edit = action.occurrenceEdits.single()
            assertThat(edit.roomId).isEqualTo(AN_EVENT_ROOM_ID)
            assertThat(edit.occurrenceStart).isEqualTo(occurrenceStart)
            assertThat(edit.form.title).isEqualTo("Only this one")
        }
    }

    @Test
    fun `present - a cancelled event ignores edit and cancel`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID), isCancelled = true)
        val action = FakeUpdateCalendarEventAction()
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            details.eventSink(CalendarEventDetailsEvent.Edit)
            details.eventSink(CalendarEventDetailsEvent.Cancel)
            details.eventSink(CalendarEventDetailsEvent.ChooseScope(EventScope.Series))
            details.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            cancelAndIgnoreRemainingEvents()
            assertThat(action.cancellations).isEmpty()
            assertThat(action.seriesEdits).isEmpty()
        }
    }

    @Test
    fun `present - a pending dialog is dropped when the event gets cancelled meanwhile`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(organizer = A_SESSION_ID))
        val repository = FakeCalendarEventRepository(persistentListOf(entry))
        val action = FakeUpdateCalendarEventAction()
        createPresenter(calendarEventRepository = repository, updateCalendarEventAction = action).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            val confirming = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            repository.entries.value = persistentListOf(entry.copy(isCancelled = true))
            val cancelled = awaitState { it.eventDetails?.entry?.isCancelled == true }.eventDetails!!
            assertThat(cancelled.pendingAction).isNull()
            // A confirmation from the stale dialog does nothing.
            confirming.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            cancelAndIgnoreRemainingEvents()
            assertThat(action.cancellations).isEmpty()
        }
    }

    @Test
    fun `present - an entry without a known occurrence is never changed as a single occurrence`() = runTest {
        // Its rule is known but not which occurrence it is, so it is managed as a whole.
        val entry = aCalendarEntry(id = "mine", rrule = "FREQ=DAILY", occurrenceStart = null, room = aCalendarEntryRoom(organizer = A_SESSION_ID))
        val action = FakeUpdateCalendarEventAction()
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            val details = awaitState { it.eventDetails != null }.eventDetails!!
            // No dialog is waiting, so neither of these does anything.
            details.eventSink(CalendarEventDetailsEvent.ChooseScope(EventScope.Occurrence))
            details.eventSink(CalendarEventDetailsEvent.ConfirmCancel)
            details.eventSink(CalendarEventDetailsEvent.Cancel)
            val confirming = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            assertThat(confirming.pendingAction).isEqualTo(ManageAction.ConfirmCancel(EventScope.Series))
            assertThat(action.cancellations).isEmpty()
        }
    }

    @Test
    fun `present - an unsupported rule is managed as a whole event`() = runTest {
        val entry = aCalendarEntry(
            id = "mine",
            rrule = "FREQ=WEEKLY;INTERVAL=2;BYDAY=TH",
            occurrenceStart = LocalDateTime.of(2026, 10, 1, 14, 0),
            room = aCalendarEntryRoom(organizer = A_SESSION_ID),
        )
        createPresenter(calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry))).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Cancel)
            val confirming = awaitState { it.eventDetails?.pendingAction != null }.eventDetails!!
            assertThat(confirming.pendingAction).isEqualTo(ManageAction.ConfirmCancel(EventScope.Series))
        }
    }

    @Test
    fun `present - an event that cannot be read for editing says so`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        val snackbarDispatcher = SnackbarDispatcher()
        // The room is not available, so its event cannot be read.
        createPresenter(
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId).isEqualTo(R.string.screen_calendar_event_error_load)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - an edit that could not update everyone says so`() = runTest {
        val entry = aCalendarEntry(id = "mine", room = aCalendarEntryRoom(roomId = AN_EVENT_ROOM_ID, organizer = A_SESSION_ID))
        val action = FakeUpdateCalendarEventAction(notUpdated = listOf(UserId("@bob:example.org")))
        val snackbarDispatcher = SnackbarDispatcher()
        createPresenter(
            client = aClientWithEventRoom(entry),
            calendarEventRepository = FakeCalendarEventRepository(persistentListOf(entry)),
            updateCalendarEventAction = action,
            snackbarDispatcher = snackbarDispatcher,
        ).test {
            awaitState { it.entries.isNotEmpty() }.eventSink(CalendarEvent.OpenEntry(entry))
            awaitState { it.eventDetails != null }.eventDetails!!.eventSink(CalendarEventDetailsEvent.Edit)
            awaitState { it.eventForm != null }.eventForm!!.eventSink(EventFormEvent.SetTitle("Renamed"))
            awaitState { it.eventForm?.form?.title == "Renamed" }.eventForm!!.eventSink(EventFormEvent.Save)
            awaitState { it.eventForm == null }
            assertThat(action.seriesEdits.single().initialParticipants).containsExactly(UserId("@bob:example.org"))
            assertThat(snackbarDispatcher.snackbarMessage.first()?.messageResId)
                .isEqualTo(R.string.screen_calendar_event_updated_some_not_updated)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** A client holding the room of [entry], whose topic carries the entry's schedule and whose members are known. */
    private fun aClientWithEventRoom(
        entry: CalendarEntry,
        seriesStart: LocalDateTime = entry.start,
        seriesEnd: LocalDateTime = entry.end,
    ): FakeMatrixClient {
        val schedule = CalendarSchedule(
            start = seriesStart,
            end = seriesEnd,
            timeZone = ZoneId.systemDefault(),
            isAllDay = entry.isAllDay,
            rrule = entry.rrule,
        )
        val roomId = entry.room!!.roomId
        val baseRoom = FakeBaseRoom(
            roomId = roomId,
            initialRoomInfo = aRoomInfo(id = roomId, name = entry.title, topic = CalendarRoomEncoding.encode("Plan the sprint", schedule)),
            updateMembersResult = {},
            roomPermalinkResult = { Result.success("https://matrix.to/#/$roomId") },
        ).apply {
            givenRoomMembersState(
                RoomMembersState.Ready(
                    listOf(aRoomMember(userId = A_SESSION_ID), aRoomMember(userId = UserId("@bob:example.org"))).toImmutableList()
                )
            )
        }
        return FakeMatrixClient().apply { givenGetRoomResult(roomId, FakeJoinedRoom(baseRoom = baseRoom)) }
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
