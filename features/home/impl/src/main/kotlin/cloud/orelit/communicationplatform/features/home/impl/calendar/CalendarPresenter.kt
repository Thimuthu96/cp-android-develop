/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.features.call.api.ElementCallEntryPoint
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormMode
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormState
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.ParticipantPickerState
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarEventRepository
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CalendarRoomEncoding
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.CreateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.EditedCalendarEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.UpdateCalendarEventAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.eventParticipants
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.loadMembersOrNull
import cloud.orelit.communicationplatform.features.home.impl.calendar.data.moveZone
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsState
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.EventScope
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.ManageAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.toMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserRepository
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResultState
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

private const val SEARCH_DEBOUNCE_MILLIS = 300L

/** How far the calendar reads around today, wide enough for the agenda list and the pagers. */
private const val DAYS_BEFORE_TODAY = 60L
private const val DAYS_AFTER_TODAY = 180L

/** The times an all-day event gets in its form, should the user switch the option off. */
private val ALL_DAY_FORM_START: LocalTime = LocalTime.of(9, 0)
private val ALL_DAY_FORM_END: LocalTime = LocalTime.of(10, 0)

/** Only whoever created the event's room may change it, like only the organiser can in Teams. */
private fun CalendarEntry.canBeManagedBy(userId: UserId): Boolean = room?.organizer?.let { it == userId } == true

/**
 * True when this entry is one occurrence of a series the calendar expands, so a change may apply to it alone.
 * A rule the calendar cannot expand is shown as a single event, and is changed as a whole.
 */
private fun CalendarEntry.isRecurring(): Boolean = occurrenceStart != null && CalendarRecurrence.isSupported(rrule)

private val NoSearchResult = UserSearchResultState(results = emptyList(), isSearching = false)

@Inject
class CalendarPresenter(
    private val client: MatrixClient,
    private val userRepository: UserRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val createCalendarEventAction: CreateCalendarEventAction,
    private val updateCalendarEventAction: UpdateCalendarEventAction,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<CalendarState> {
    @Composable
    override fun present(): CalendarState {
        val coroutineScope = rememberCoroutineScope()
        var selectedEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
        var viewModeOrdinal by rememberSaveable { mutableIntStateOf(CalendarViewMode.Day.ordinal) }

        val now by produceState(initialValue = LocalDateTime.now()) {
            while (true) {
                // Wake up at the start of the next minute.
                delay((60 - value.second) * 1_000L)
                value = LocalDateTime.now()
            }
        }
        val firstDayOfWeek = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }

        val today = now.toLocalDate()
        val entries by remember(today) {
            calendarEventRepository.entriesFlow(around = today, daysBefore = DAYS_BEFORE_TODAY, daysAfter = DAYS_AFTER_TODAY)
        }.collectAsState(initial = persistentListOf())

        // The event form, null when it is closed.
        var form by remember { mutableStateOf<NewEventForm?>(null) }
        var formMode by remember { mutableStateOf<EventFormMode>(EventFormMode.Create) }
        // What an edited event held when its form was opened, to tell whether anything changed.
        var initialForm by remember { mutableStateOf<NewEventForm?>(null) }
        var saveAction by remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        var isPickingParticipants by remember { mutableStateOf(false) }
        var participantQuery by remember { mutableStateOf("") }
        val participantSearch by remember(isPickingParticipants, participantQuery) {
            if (isPickingParticipants) searchUsers(participantQuery) else flowOf(NoSearchResult)
        }.collectAsState(initial = NoSearchResult)

        // The entry whose details are shown, kept by id so it follows the entries as the rooms sync.
        var openEntryId by remember { mutableStateOf<String?>(null) }
        val openEntry = remember(openEntryId, entries) { entries.firstOrNull { it.id == openEntryId } }
        val organizer = rememberOrganizer(openEntry?.room?.organizer)
        val meetingLink = rememberMeetingLink(openEntry?.room?.roomId)
        // The dialog the edit or cancel flow of the open event waits on.
        var pendingAction by remember { mutableStateOf<ManageAction?>(null) }
        // Reading the event before its form opens, so a second tap does not load it twice.
        val editLoadJob = remember { mutableStateOf<Job?>(null) }
        // A dialog about an event that got cancelled meanwhile, or is no longer shown, would act on stale state.
        val openEntryIsCancelled = openEntry?.isCancelled == true
        LaunchedEffect(openEntryIsCancelled) {
            if (openEntryIsCancelled) pendingAction = null
        }

        fun closeForm() {
            form = null
            formMode = EventFormMode.Create
            initialForm = null
            saveAction = AsyncAction.Uninitialized
            isPickingParticipants = false
            participantQuery = ""
        }

        fun save() {
            val currentForm = form ?: return
            if (!currentForm.canSave || saveAction is AsyncAction.Loading) return
            val mode = formMode
            // An edit that changes nothing has nothing to save.
            if (mode !is EventFormMode.Create && currentForm == initialForm) return
            if (mode !is EventFormMode.Create) {
                coroutineScope.launch {
                    saveAction = AsyncAction.Loading
                    val result = when (mode) {
                        is EventFormMode.EditSeries -> updateCalendarEventAction.editSeries(
                            roomId = mode.roomId,
                            form = currentForm,
                            initialParticipants = initialForm?.participants.orEmpty().map { it.userId },
                        )
                        is EventFormMode.EditOccurrence -> updateCalendarEventAction.editOccurrence(mode.roomId, mode.occurrenceStart, currentForm)
                            .map { EditedCalendarEvent() }
                        EventFormMode.Create -> return@launch
                    }
                    result.fold(
                        onSuccess = { edited ->
                            closeForm()
                            // The event is saved either way; saying so only when everyone was updated would hide the gap.
                            snackbarDispatcher.post(
                                if (edited.notUpdated.isEmpty()) {
                                    SnackbarMessage(R.string.screen_calendar_event_updated)
                                } else {
                                    SnackbarMessage(R.string.screen_calendar_event_updated_some_not_updated)
                                }
                            )
                        },
                        onFailure = { saveAction = AsyncAction.Failure(it) },
                    )
                }
                return
            }
            coroutineScope.launch {
                saveAction = AsyncAction.Loading
                createCalendarEventAction.execute(currentForm).fold(
                    onSuccess = { created ->
                        closeForm()
                        // The event exists either way; saying so only when everyone was invited would hide the gap.
                        snackbarDispatcher.post(
                            if (created.notInvited.isEmpty()) {
                                SnackbarMessage(R.string.screen_calendar_event_sent)
                            } else {
                                SnackbarMessage(R.string.screen_calendar_event_created_some_not_invited)
                            }
                        )
                    },
                    onFailure = { saveAction = AsyncAction.Failure(it) },
                )
            }
        }

        fun joinCall(roomId: RoomId?) {
            if (roomId == null) {
                snackbarDispatcher.post(SnackbarMessage(R.string.screen_calendar_event_error_no_room))
                return
            }
            openEntryId = null
            elementCallEntryPoint.startCall(
                CallData(sessionId = client.sessionId, roomId = roomId, isAudioCall = false)
            )
        }

        fun handleFormEvent(event: EventFormEvent) {
            // A single occurrence keeps the repeat rule, the participants and the kind of its series.
            val changesSeries = event is EventFormEvent.SetAllDay || event is EventFormEvent.SetRecurrence ||
                event is EventFormEvent.OpenParticipantPicker || event is EventFormEvent.ToggleParticipant ||
                event is EventFormEvent.RemoveParticipant
            if (changesSeries && formMode is EventFormMode.EditOccurrence) return
            when (event) {
                is EventFormEvent.SetTitle -> form = form?.copy(title = event.title)
                is EventFormEvent.SetDescription -> form = form?.copy(description = event.description)
                is EventFormEvent.SetAllDay -> form = form?.copy(isAllDay = event.isAllDay)
                is EventFormEvent.SetStart -> form = form?.withStart(event.start)
                is EventFormEvent.SetEnd -> form = form?.withEnd(event.end)
                is EventFormEvent.SetRecurrence -> form = form?.copy(recurrence = event.recurrence)
                EventFormEvent.OpenParticipantPicker -> isPickingParticipants = true
                EventFormEvent.CloseParticipantPicker -> {
                    isPickingParticipants = false
                    participantQuery = ""
                }
                is EventFormEvent.SetParticipantQuery -> participantQuery = event.query
                is EventFormEvent.ToggleParticipant -> form = form?.let { current ->
                    if (current.participants.any { it.userId == event.user.userId }) {
                        current.withoutParticipant(event.user)
                    } else {
                        current.withParticipant(event.user)
                    }
                }
                is EventFormEvent.RemoveParticipant -> form = form?.withoutParticipant(event.user)
                EventFormEvent.Save -> save()
                EventFormEvent.ClearSaveError -> saveAction = AsyncAction.Uninitialized
                EventFormEvent.Dismiss -> closeForm()
            }
        }

        fun startEdit(entry: CalendarEntry, scope: EventScope) {
            pendingAction = null
            val roomId = entry.room?.roomId ?: return
            // An occurrence can only be edited on its own when it is known which one it is.
            val mode = when (scope) {
                EventScope.Series -> EventFormMode.EditSeries(roomId)
                EventScope.Occurrence -> EventFormMode.EditOccurrence(roomId, entry.occurrenceStart ?: return)
            }
            if (editLoadJob.value?.isActive == true) return
            editLoadJob.value = coroutineScope.launch {
                val prefilled = runCatchingExceptions { loadEditForm(entry, scope) }
                    .onFailure { Timber.e(it, "Could not load the event in $roomId for editing") }
                    .getOrNull()
                if (prefilled == null) {
                    snackbarDispatcher.post(SnackbarMessage(R.string.screen_calendar_event_error_load))
                    return@launch
                }
                formMode = mode
                initialForm = prefilled
                form = prefilled
            }
        }

        fun cancelEdit() {
            editLoadJob.value?.cancel()
            editLoadJob.value = null
        }

        fun cancelEvent(entry: CalendarEntry, scope: EventScope) {
            pendingAction = null
            val roomId = entry.room?.roomId ?: return
            // Never falls back to the whole event: calling off more than the organiser picked cannot be undone.
            val occurrenceStart = when (scope) {
                EventScope.Series -> null
                EventScope.Occurrence -> entry.occurrenceStart ?: return
            }
            coroutineScope.launch {
                updateCalendarEventAction.cancel(roomId, occurrenceStart).fold(
                    onSuccess = { snackbarDispatcher.post(SnackbarMessage(R.string.screen_calendar_event_canceled)) },
                    onFailure = { snackbarDispatcher.post(SnackbarMessage(R.string.screen_calendar_event_cancel_failed)) },
                )
            }
        }

        fun handleDetailsEvent(event: CalendarEventDetailsEvent) {
            val entry = openEntry
            val roomId = entry?.room?.roomId
            // Checked on every event rather than trusted from the view: the event may have been cancelled meanwhile.
            val manageable = entry?.takeIf { it.canBeManagedBy(client.sessionId) && !it.isCancelled }
            when (event) {
                CalendarEventDetailsEvent.JoinCall -> if (entry?.isCancelled != true) joinCall(roomId)
                // The view holds the clipboard, so by the time this arrives the link is already copied.
                CalendarEventDetailsEvent.CopyLink -> snackbarDispatcher.post(SnackbarMessage(R.string.screen_calendar_event_link_copied))
                CalendarEventDetailsEvent.Dismiss -> {
                    cancelEdit()
                    openEntryId = null
                    pendingAction = null
                }
                CalendarEventDetailsEvent.Edit -> when {
                    manageable == null -> pendingAction = null
                    manageable.isRecurring() -> pendingAction = ManageAction.AskScope(ManageAction.Kind.Edit)
                    else -> startEdit(manageable, EventScope.Series)
                }
                CalendarEventDetailsEvent.Cancel -> pendingAction = when {
                    manageable == null -> null
                    manageable.isRecurring() -> ManageAction.AskScope(ManageAction.Kind.Cancel)
                    else -> ManageAction.ConfirmCancel(EventScope.Series)
                }
                is CalendarEventDetailsEvent.ChooseScope -> {
                    val pending = pendingAction as? ManageAction.AskScope
                    when {
                        manageable == null || pending == null -> pendingAction = null
                        pending.kind == ManageAction.Kind.Edit -> startEdit(manageable, event.scope)
                        else -> pendingAction = ManageAction.ConfirmCancel(event.scope)
                    }
                }
                CalendarEventDetailsEvent.ConfirmCancel -> {
                    val pending = pendingAction as? ManageAction.ConfirmCancel
                    if (manageable != null && pending != null) cancelEvent(manageable, pending.scope) else pendingAction = null
                }
                CalendarEventDetailsEvent.DismissDialog -> pendingAction = null
            }
        }

        fun openForm(start: LocalDateTime) {
            // The details of another event would end up behind the form.
            openEntryId = null
            pendingAction = null
            formMode = EventFormMode.Create
            initialForm = null
            form = NewEventForm.initial(date = start.toLocalDate(), now = now).withStart(start)
        }

        fun handleEvent(event: CalendarEvent) {
            when (event) {
                is CalendarEvent.SelectDate -> selectedEpochDay = event.date.toEpochDay()
                is CalendarEvent.SelectViewMode -> viewModeOrdinal = event.mode.ordinal
                CalendarEvent.GoToToday -> selectedEpochDay = LocalDate.now().toEpochDay()
                CalendarEvent.NewEvent -> {
                    val date = LocalDate.ofEpochDay(selectedEpochDay)
                    openForm(NewEventForm.initial(date = date, now = now).start)
                }
                is CalendarEvent.NewEventAt -> openForm(event.start)
                is CalendarEvent.OpenEntry -> {
                    if (event.entry.id != openEntryId) cancelEdit()
                    openEntryId = event.entry.id
                    pendingAction = null
                }
            }
        }

        val eventForm = form?.let { currentForm ->
            EventFormState(
                form = currentForm,
                saveAction = saveAction,
                participantPicker = if (isPickingParticipants) {
                    ParticipantPickerState(
                        query = participantQuery,
                        results = participantSearch.results.filterNot { it.matrixUser.userId.value == client.sessionId.value }.toImmutableList(),
                        isSearching = participantSearch.isSearching,
                    )
                } else {
                    null
                },
                mode = formMode,
                initialForm = initialForm,
                eventSink = ::handleFormEvent,
            )
        }

        // The sheet is a window of its own and would cover the form, so it waits behind it until the form closes.
        val eventDetails = openEntry?.takeIf { form == null }?.let { entry ->
            CalendarEventDetailsState(
                entry = entry,
                organizer = organizer,
                participants = entry.room?.participants ?: persistentListOf(),
                participantCount = entry.room?.participantCount ?: 0L,
                hasActiveCall = entry.room?.hasActiveCall == true,
                meetingLink = meetingLink,
                canManage = entry.canBeManagedBy(client.sessionId),
                pendingAction = pendingAction.takeUnless { entry.isCancelled },
                eventSink = ::handleDetailsEvent,
            )
        }

        return CalendarState(
            selectedDate = LocalDate.ofEpochDay(selectedEpochDay),
            viewMode = CalendarViewMode.from(viewModeOrdinal),
            now = now,
            firstDayOfWeek = firstDayOfWeek,
            entries = entries,
            eventForm = eventForm,
            eventDetails = eventDetails,
            eventSink = ::handleEvent,
        )
    }

    /**
     * The shareable link to the meeting, resolved while its details are open.
     *
     * The SDK builds the permalink so it matches what every other client produces; it is null until it arrives,
     * which is what keeps the copy action disabled.
     */
    @Composable
    private fun rememberMeetingLink(roomId: RoomId?): String? {
        var link by remember(roomId) { mutableStateOf<String?>(null) }
        LaunchedEffect(roomId) {
            if (roomId == null) return@LaunchedEffect
            client.getJoinedRoom(roomId)?.use { room ->
                room.getPermalink()
                    .onSuccess { link = it }
                    .onFailure { Timber.w(it, "Could not build the permalink of the meeting") }
            }
        }
        return link
    }

    /** The profile of the organiser, which the room list only knows the id of. */
    @Composable
    private fun rememberOrganizer(userId: UserId?): MatrixUser? {
        var organizer by remember(userId) { mutableStateOf(userId?.let { MatrixUser(userId = it) }) }
        LaunchedEffect(userId) {
            if (userId == null) return@LaunchedEffect
            client.getProfile(userId).onSuccess { organizer = it }
        }
        return organizer
    }

    /**
     * The form prefilled with the event as it is now, or null when the event or, for a whole event, its
     * participants cannot be read.
     *
     * A whole event is read from its room rather than from the entry, so it is not a sync behind, and its times are
     * those of the series. A single occurrence is prefilled from the entry, which already has that occurrence's own
     * times, title and description. The participants of a whole event must be known for sure: they are what saving
     * compares with to find who was taken out.
     */
    private suspend fun loadEditForm(entry: CalendarEntry, scope: EventScope): NewEventForm? {
        val roomId = entry.room?.roomId ?: return null
        val deviceZone = ZoneId.systemDefault()
        val room = client.getJoinedRoom(roomId) ?: return null
        return room.use {
            val info = room.info()
            val schedule = CalendarRoomEncoding.decodeSchedule(info.topic) ?: return null
            val members = room.loadMembersOrNull()
            if (members == null && scope == EventScope.Series) {
                Timber.w("The members of $roomId are not known, the event cannot be edited")
                return null
            }
            val participants = members.orEmpty()
                .eventParticipants(client.sessionId)
                .map { it.toMatrixUser() }
                .toImmutableList()
            val (start, end) = if (scope == EventScope.Series) {
                if (schedule.isAllDay) {
                    schedule.start to schedule.end
                } else {
                    schedule.start.moveZone(schedule.timeZone, deviceZone) to schedule.end.moveZone(schedule.timeZone, deviceZone)
                }
            } else {
                entry.start to entry.end
            }
            val formStart = if (schedule.isAllDay) start.toLocalDate().atTime(ALL_DAY_FORM_START) else start
            NewEventForm(
                title = if (scope == EventScope.Series) info.name?.takeIf { it.isNotBlank() } ?: entry.title else entry.title,
                description = if (scope == EventScope.Series) CalendarRoomEncoding.decodeDescription(info.topic).orEmpty() else entry.description.orEmpty(),
                isAllDay = schedule.isAllDay,
                start = formStart,
                // The form keeps the last day of an all-day event, while the schedule ends at the midnight after it.
                // A zero-length all-day event would otherwise end the day before it starts.
                end = if (schedule.isAllDay) {
                    maxOf(end.toLocalDate().minusDays(1), formStart.toLocalDate()).atTime(ALL_DAY_FORM_END)
                } else {
                    end
                },
                recurrence = EventRecurrence.fromRRule(schedule.rrule),
                participants = participants,
            )
        }
    }

    private fun searchUsers(query: String): Flow<UserSearchResultState> {
        if (query.isBlank()) return flowOf(NoSearchResult)
        return flow {
            // The search is started again on each key stroke, wait for the user to pause typing.
            delay(SEARCH_DEBOUNCE_MILLIS)
            emitAll(userRepository.search(query))
        }
    }
}
