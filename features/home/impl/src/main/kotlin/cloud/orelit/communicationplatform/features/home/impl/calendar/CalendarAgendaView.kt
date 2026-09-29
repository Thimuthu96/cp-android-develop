/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private const val AGENDA_DAYS_BEFORE_TODAY = 30L
private const val AGENDA_DAY_COUNT = 30 + 120

/** The agenda: a week strip on top of a list of days, each of them listing its entries. */
@Composable
internal fun CalendarAgendaView(
    state: CalendarState,
    formatters: CalendarFormatters,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        CalendarWeekStrip(state = state, formatters = formatters)
        HorizontalDivider()
        if (state.entries.isEmpty()) {
            CalendarEmptyState(modifier = Modifier.weight(1f))
        } else {
            CalendarAgendaList(
                state = state,
                formatters = formatters,
                contentPadding = PaddingValues(bottom = bottomPadding),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarAgendaList(
    state: CalendarState,
    formatters: CalendarFormatters,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val firstDate = remember { state.today.minusDays(AGENDA_DAYS_BEFORE_TODAY) }
    fun indexOf(date: LocalDate) = ChronoUnit.DAYS.between(firstDate, date).toInt().coerceIn(0, AGENDA_DAY_COUNT - 1)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = indexOf(state.selectedDate))
    val latestSelectedDate by rememberUpdatedState(state.selectedDate)
    val latestEventSink by rememberUpdatedState(state.eventSink)
    // Scrolls triggered by a change of the selected date must not select the dates they go through.
    var isScrollingToSelection by remember { mutableStateOf(false) }

    LaunchedEffect(state.selectedDate) {
        val target = indexOf(state.selectedDate)
        if (listState.firstVisibleItemIndex != target) {
            isScrollingToSelection = true
            try {
                listState.animateScrollToItem(target)
            } finally {
                isScrollingToSelection = false
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            val date = firstDate.plusDays(index.toLong())
            if (!isScrollingToSelection && date != latestSelectedDate) {
                latestEventSink(CalendarEvent.SelectDate(date))
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(count = AGENDA_DAY_COUNT, key = { it }) { index ->
            val date = firstDate.plusDays(index.toLong())
            val entries = remember(state.entries, date) {
                state.entriesOn(date)
                    .sortedWith(compareByDescending<CalendarEntry> { it.isAllDay }.thenBy { it.start })
                    .toImmutableList()
            }
            CalendarAgendaDay(
                date = date,
                today = state.today,
                entries = entries,
                formatters = formatters,
                eventSink = state.eventSink,
            )
        }
    }
}

@Composable
private fun CalendarAgendaDay(
    date: LocalDate,
    today: LocalDate,
    entries: ImmutableList<CalendarEntry>,
    formatters: CalendarFormatters,
    eventSink: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val isToday = date == today
        val title = when (date) {
            today -> stringResource(R.string.screen_calendar_today)
            today.plusDays(1) -> stringResource(R.string.screen_calendar_tomorrow)
            else -> formatters.weekdayLong(date)
        }
        Row(
            modifier = Modifier.semantics { heading() },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = ElementTheme.typography.fontHeadingSmMedium,
                color = if (isToday) ElementTheme.colors.textActionAccent else ElementTheme.colors.textPrimary,
            )
            Text(
                text = formatters.monthDay(date),
                modifier = Modifier.padding(bottom = 1.dp),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.screen_calendar_no_events_day),
                modifier = Modifier
                    .fillMaxWidth()
                    // A free day is where a user reaches to fill it, so tapping it starts an event.
                    .clickable { eventSink(CalendarEvent.NewEventAt(date.atTime(DEFAULT_NEW_EVENT_TIME))) }
                    .padding(vertical = 4.dp),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
        } else {
            entries.forEach { entry ->
                CalendarAgendaEntry(
                    entry = entry,
                    formatters = formatters,
                    onClick = { eventSink(CalendarEvent.OpenEntry(entry)) },
                )
            }
        }
    }
}

@Composable
private fun CalendarAgendaEntry(
    entry: CalendarEntry,
    formatters: CalendarFormatters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // A cancelled entry is greyed out as a whole, its times included.
        val primaryColor = if (entry.isCancelled) ElementTheme.colors.textDisabled else ElementTheme.colors.textPrimary
        val secondaryColor = if (entry.isCancelled) ElementTheme.colors.textDisabled else ElementTheme.colors.textSecondary
        Column(modifier = Modifier.width(64.dp)) {
            if (entry.isAllDay) {
                Text(
                    text = stringResource(R.string.screen_calendar_all_day),
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = primaryColor,
                )
            } else {
                Text(
                    text = formatters.time(entry.start.toLocalTime()),
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = primaryColor,
                )
                Text(
                    text = formatters.time(entry.end.toLocalTime()),
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = secondaryColor,
                )
            }
        }
        CalendarEntryCard(
            entry = entry,
            timeRange = formatters.timeRange(entry),
            compact = false,
            showDetails = !entry.isAllDay,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun CalendarEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = CompoundIcons.Calendar(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.screen_calendar_empty_title),
            modifier = Modifier.padding(top = 16.dp),
            style = ElementTheme.typography.fontHeadingSmMedium,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.screen_calendar_empty_subtitle),
            modifier = Modifier.padding(top = 4.dp),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Where an event started from an empty agenda day begins, matching the form's own default. */
private val DEFAULT_NEW_EVENT_TIME: LocalTime = LocalTime.of(9, 0)
