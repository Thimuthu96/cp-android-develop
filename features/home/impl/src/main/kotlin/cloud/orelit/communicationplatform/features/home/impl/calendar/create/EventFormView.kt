/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarFormatters
import cloud.orelit.communicationplatform.features.home.impl.calendar.rememberCalendarFormatters
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.DropdownMenu
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.DropdownMenuItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSectionHeader
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Switch
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextField
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SelectedUsersRowList
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.flow.drop
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/** Which part of the start or the end of the event the user is editing with a picker. */
private data class PickerTarget(val isStart: Boolean, val isDate: Boolean)

/**
 * The full screen form to create or edit an event. Saving a new event creates its room, see
 * `CreateCalendarEventAction`, while saving an edit rewrites it, see `UpdateCalendarEventAction`.
 *
 * A single occurrence of a series can only change its own title, times and description, so the rows that would
 * change the whole series are disabled then.
 */
@Composable
fun EventFormView(
    state: EventFormState,
    modifier: Modifier = Modifier,
) {
    val form = state.form
    val eventSink = state.eventSink
    val latestEventSink by rememberUpdatedState(eventSink)
    val formatters = rememberCalendarFormatters()

    // The fields keep their own text and selection. Feeding them the presenter's value instead would move the
    // caret back to the start on the first keystroke, because that value only catches up on the next frame.
    val titleState = rememberTextFieldState(initialText = form.title)
    val descriptionState = rememberTextFieldState(initialText = form.description)
    LaunchedEffect(titleState) {
        // The first value is the one the field was created with, the presenter already has it.
        snapshotFlow { titleState.text.toString() }.drop(1).collect { latestEventSink(EventFormEvent.SetTitle(it)) }
    }
    LaunchedEffect(descriptionState) {
        snapshotFlow { descriptionState.text.toString() }.drop(1).collect { latestEventSink(EventFormEvent.SetDescription(it)) }
    }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var showDiscardConfirmation by remember { mutableStateOf(false) }

    fun requestClose() {
        when {
            state.isSaving -> Unit
            state.isDirty -> showDiscardConfirmation = true
            else -> eventSink(EventFormEvent.Dismiss)
        }
    }

    // The participant picker has its own handler, as it is displayed on top of this screen.
    BackHandler(enabled = state.participantPicker == null) { requestClose() }

    Box(modifier.background(ElementTheme.colors.bgCanvasDefault)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    titleStr = stringResource(if (state.isEditing) R.string.screen_calendar_edit_event else R.string.screen_calendar_new_event),
                    navigationIcon = {
                        IconButton(onClick = ::requestClose) {
                            Icon(
                                imageVector = CompoundIcons.Close(),
                                contentDescription = stringResource(CommonStrings.action_close),
                            )
                        }
                    },
                    actions = {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            TextButton(
                                text = stringResource(CommonStrings.action_save),
                                onClick = { eventSink(EventFormEvent.Save) },
                                enabled = state.canSave,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                TextField(
                    state = titleState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    label = stringResource(R.string.screen_calendar_event_title_label),
                    placeholder = stringResource(R.string.screen_calendar_event_title_hint),
                    enabled = !state.isSaving,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                )

                ListSectionHeader(title = stringResource(R.string.screen_calendar_event_section_when))
                ListItem(
                    content = { Text(stringResource(R.string.screen_calendar_all_day)) },
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Time())),
                    trailingContent = ListItemContent.Switch(checked = form.isAllDay, enabled = !state.isSaving && !state.isEditingOccurrence),
                    enabled = !state.isSaving && !state.isEditingOccurrence,
                    onClick = { eventSink(EventFormEvent.SetAllDay(!form.isAllDay)) },
                )
                EventFormDateTimeRow(
                    label = stringResource(R.string.screen_calendar_event_starts),
                    value = form.start,
                    isAllDay = form.isAllDay,
                    formatters = formatters,
                    enabled = !state.isSaving,
                    onDateClick = { pickerTarget = PickerTarget(isStart = true, isDate = true) },
                    onTimeClick = { pickerTarget = PickerTarget(isStart = true, isDate = false) },
                )
                EventFormDateTimeRow(
                    label = stringResource(R.string.screen_calendar_event_ends),
                    value = form.end,
                    isAllDay = form.isAllDay,
                    formatters = formatters,
                    enabled = !state.isSaving,
                    isError = !form.isTimeRangeValid,
                    onDateClick = { pickerTarget = PickerTarget(isStart = false, isDate = true) },
                    onTimeClick = { pickerTarget = PickerTarget(isStart = false, isDate = false) },
                )
                if (!form.isTimeRangeValid) {
                    Text(
                        text = stringResource(R.string.screen_calendar_event_end_before_start),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textCriticalPrimary,
                    )
                }
                EventFormRecurrenceRow(
                    recurrence = form.recurrence,
                    enabled = !state.isSaving && !state.isEditingOccurrence,
                    onSelect = { eventSink(EventFormEvent.SetRecurrence(it)) },
                )

                ListSectionHeader(title = stringResource(R.string.screen_calendar_event_section_people))
                ListItem(
                    content = { Text(stringResource(R.string.screen_calendar_event_add_people)) },
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.UserAdd())),
                    enabled = !state.isSaving && !state.isEditingOccurrence,
                    onClick = { eventSink(EventFormEvent.OpenParticipantPicker) },
                )
                if (form.participants.isNotEmpty()) {
                    SelectedUsersRowList(
                        selectedUsers = form.participants,
                        onUserRemove = { eventSink(EventFormEvent.RemoveParticipant(it)) },
                        canDeselect = { !state.isEditingOccurrence },
                        modifier = Modifier.padding(bottom = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    )
                }

                ListSectionHeader(title = stringResource(R.string.screen_calendar_event_description))
                TextField(
                    state = descriptionState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    placeholder = stringResource(R.string.screen_calendar_event_description_hint),
                    enabled = !state.isSaving,
                    lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3, maxHeightInLines = 6),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                // Keep the last field clear of the keyboard and of the navigation bar.
                Spacer(Modifier.size(24.dp))
            }
        }

        state.participantPicker?.let { picker ->
            ParticipantPickerView(
                form = form,
                picker = picker,
                eventSink = eventSink,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    pickerTarget?.let { target ->
        val current = if (target.isStart) form.start else form.end
        val onPicked = { picked: LocalDateTime ->
            eventSink(if (target.isStart) EventFormEvent.SetStart(picked) else EventFormEvent.SetEnd(picked))
            pickerTarget = null
        }
        if (target.isDate) {
            EventDatePickerDialog(
                initialDate = current.toLocalDate(),
                onConfirm = { date -> onPicked(date.atTime(current.toLocalTime())) },
                onDismiss = { pickerTarget = null },
            )
        } else {
            EventTimePickerDialog(
                initialTime = current.toLocalTime(),
                onConfirm = { time -> onPicked(current.toLocalDate().atTime(time)) },
                onDismiss = { pickerTarget = null },
            )
        }
    }

    if (showDiscardConfirmation) {
        ConfirmationDialog(
            title = stringResource(
                if (state.isEditing) R.string.screen_calendar_event_discard_changes_title else R.string.screen_calendar_event_discard_title
            ),
            content = stringResource(
                if (state.isEditing) R.string.screen_calendar_event_discard_changes_message else R.string.screen_calendar_event_discard_message
            ),
            submitText = stringResource(CommonStrings.action_discard),
            destructiveSubmit = true,
            onSubmitClick = {
                showDiscardConfirmation = false
                eventSink(EventFormEvent.Dismiss)
            },
            onDismiss = { showDiscardConfirmation = false },
        )
    }

    (state.saveAction as? AsyncAction.Failure)?.let { failure ->
        val generic = stringResource(if (state.isEditing) R.string.screen_calendar_event_error_update else R.string.screen_calendar_event_error_generic)
        ErrorDialog(
            content = when (failure.error) {
                is MeetingsNotConfiguredException -> stringResource(R.string.screen_calendar_event_error_not_configured)
                is MeetingsBotUnavailableException -> stringResource(R.string.screen_calendar_event_error_bot_unavailable)
                // What the homeserver said is far more useful than "try again", which is never actionable on its own.
                else -> failure.error.message?.takeIf { it.isNotBlank() }?.let { "$generic\n\n$it" } ?: generic
            },
            onSubmit = { eventSink(EventFormEvent.ClearSaveError) },
        )
    }
}

@Composable
private fun EventFormDateTimeRow(
    label: String,
    value: LocalDateTime,
    isAllDay: Boolean,
    formatters: CalendarFormatters,
    enabled: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    ListItem(
        modifier = modifier,
        content = { Text(label) },
        // The date and the time open different pickers, so each is its own target rather than the whole row.
        trailingContent = ListItemContent.Custom { isEnabled ->
            val valueColor = if (isError) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EventFormPickerValue(
                    text = formatters.date(value.toLocalDate()),
                    color = valueColor,
                    enabled = isEnabled,
                    onClick = onDateClick,
                )
                if (!isAllDay) {
                    EventFormPickerValue(
                        text = formatters.time(value.toLocalTime()),
                        color = valueColor,
                        enabled = isEnabled,
                        onClick = onTimeClick,
                    )
                }
            }
        },
        enabled = enabled,
    )
}

/** A tappable date or time in a form row, tinted like the surface it sits on so it reads as editable. */
@Composable
private fun EventFormPickerValue(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ElementTheme.colors.bgSubtleSecondary)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        style = ElementTheme.typography.fontBodyMdRegular,
        color = color,
    )
}

@Composable
private fun EventFormRecurrenceRow(
    recurrence: EventRecurrence,
    enabled: Boolean,
    onSelect: (EventRecurrence) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        ListItem(
            content = { Text(stringResource(R.string.screen_calendar_event_repeat)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Restart())),
            trailingContent = ListItemContent.Text(stringResource(recurrence.labelRes)),
            enabled = enabled,
            onClick = { showMenu = true },
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            EventRecurrence.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        showMenu = false
                        onSelect(option)
                    },
                    trailingIcon = if (option == recurrence) {
                        {
                            Icon(
                                imageVector = CompoundIcons.Check(),
                                tint = ElementTheme.colors.iconAccentPrimary,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun EventDatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    // The picker works with UTC milliseconds of the start of the day.
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = calendarPickerColors(),
        confirmButton = {
            TextButton(
                text = stringResource(CommonStrings.action_ok),
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
            )
        },
        dismissButton = {
            TextButton(text = stringResource(CommonStrings.action_cancel), onClick = onDismiss)
        },
    ) {
        DatePicker(state = pickerState, colors = calendarPickerColors())
    }
}

@Composable
private fun EventTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ElementTheme.colors.bgCanvasDefault,
        confirmButton = {
            TextButton(
                text = stringResource(CommonStrings.action_ok),
                onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) },
            )
        },
        dismissButton = {
            TextButton(text = stringResource(CommonStrings.action_cancel), onClick = onDismiss)
        },
        text = { TimePicker(state = pickerState, colors = calendarTimePickerColors()) },
    )
}

@PreviewsDayNight
@Composable
internal fun EventFormViewPreview(
    @PreviewParameter(EventFormStatePreviewParam::class) state: EventFormState,
) = ElementPreview {
    EventFormView(state = state)
}

/**
 * Compound colours for the date picker.
 *
 * Material's own defaults come from its baseline palette, which is a different purple to the one Compound uses,
 * so a picker left unstyled stands out against every other surface of the app.
 */
@Composable
private fun calendarPickerColors(): DatePickerColors = DatePickerDefaults.colors(
    containerColor = ElementTheme.colors.bgCanvasDefault,
    titleContentColor = ElementTheme.colors.textSecondary,
    headlineContentColor = ElementTheme.colors.textPrimary,
    weekdayContentColor = ElementTheme.colors.textSecondary,
    navigationContentColor = ElementTheme.colors.iconSecondary,
    yearContentColor = ElementTheme.colors.textPrimary,
    currentYearContentColor = ElementTheme.colors.textActionAccent,
    selectedYearContentColor = ElementTheme.colors.textOnSolidPrimary,
    selectedYearContainerColor = ElementTheme.colors.bgAccentRest,
    dayContentColor = ElementTheme.colors.textPrimary,
    selectedDayContentColor = ElementTheme.colors.textOnSolidPrimary,
    selectedDayContainerColor = ElementTheme.colors.bgAccentRest,
    todayContentColor = ElementTheme.colors.textActionAccent,
    todayDateBorderColor = ElementTheme.colors.borderInteractivePrimary,
    dividerColor = ElementTheme.colors.borderInteractiveSecondary,
)

/** Compound colours for the time picker, matching [calendarPickerColors]. */
@Composable
private fun calendarTimePickerColors(): TimePickerColors = TimePickerDefaults.colors(
    clockDialColor = ElementTheme.colors.bgSubtleSecondary,
    clockDialSelectedContentColor = ElementTheme.colors.textOnSolidPrimary,
    clockDialUnselectedContentColor = ElementTheme.colors.textPrimary,
    selectorColor = ElementTheme.colors.bgAccentRest,
    timeSelectorSelectedContainerColor = ElementTheme.colors.bgAccentRest,
    timeSelectorSelectedContentColor = ElementTheme.colors.textOnSolidPrimary,
    timeSelectorUnselectedContainerColor = ElementTheme.colors.bgSubtleSecondary,
    timeSelectorUnselectedContentColor = ElementTheme.colors.textPrimary,
    periodSelectorSelectedContainerColor = ElementTheme.colors.bgAccentRest,
    periodSelectorSelectedContentColor = ElementTheme.colors.textOnSolidPrimary,
    periodSelectorUnselectedContainerColor = ElementTheme.colors.bgSubtleSecondary,
    periodSelectorUnselectedContentColor = ElementTheme.colors.textPrimary,
    periodSelectorBorderColor = ElementTheme.colors.borderInteractiveSecondary,
)
