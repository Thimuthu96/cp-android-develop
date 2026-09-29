/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import java.time.LocalDate
import java.time.LocalTime

internal val CalendarHourHeight = 64.dp
private val GutterWidth = 56.dp
private val DayHeaderCircleSize = 36.dp
private val AllDayChipHeight = 22.dp

/** Above this number of visible days there is no room to show more than the title of an entry. */
private const val MAX_DAYS_WITH_ENTRY_DETAILS = 3
private val MinHeightForEntryDetails = 44.dp

/**
 * The Day, 3 days and Week views: a scrollable hour grid, with the days as columns.
 * The pager pages are [CalendarViewMode.days] days wide, the header and the grid pages swipe together.
 */
@Composable
internal fun CalendarTimeGridView(
    state: CalendarState,
    formatters: CalendarFormatters,
    scrollState: ScrollState,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val days = state.viewMode.days
    // Week pages are aligned on the first day of the week, other pages start on today.
    val anchor = remember { if (days == 7) state.today.startOfWeek(state.firstDayOfWeek) else state.today }
    val pagerState = rememberDatePagerState(
        selectedDate = state.selectedDate,
        anchor = anchor,
        step = days,
    ) { firstDate ->
        state.eventSink(CalendarEvent.SelectDate(firstDate))
    }
    // The header has a stable height while swiping: it is sized for the visible page and its neighbours.
    val allDayRows by remember(state.entries, anchor, days) {
        derivedStateOf {
            val page = pagerState.settledPage
            (page - 1..page + 1).maxOf { allDayRowCount(state.entries, pageToStartDate(it, anchor, days), days) }
        }
    }

    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(GutterWidth))
            // Not scrollable by the user: it follows the pager of the grid, which shares the same state.
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f),
            ) { page ->
                CalendarGridHeaderPage(
                    state = state,
                    formatters = formatters,
                    firstDate = pageToStartDate(page, anchor, days),
                    allDayRows = allDayRows,
                )
            }
        }
        HorizontalDivider()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CalendarHourHeight * 24),
            ) {
                CalendarHourGutter(
                    formatters = formatters,
                    modifier = Modifier
                        .width(GutterWidth)
                        .fillMaxHeight(),
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) { page ->
                    CalendarGridPage(
                        state = state,
                        formatters = formatters,
                        firstDate = pageToStartDate(page, anchor, days),
                    )
                }
            }
            Spacer(Modifier.height(bottomPadding))
        }
    }
}

@Composable
private fun CalendarGridHeaderPage(
    state: CalendarState,
    formatters: CalendarFormatters,
    firstDate: LocalDate,
    allDayRows: Int,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth()) {
        repeat(state.viewMode.days) { offset ->
            val date = firstDate.plusDays(offset.toLong())
            val allDayEntries = remember(state.entries, date) { state.entriesOn(date).filter { it.isAllDay } }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CalendarDayHeader(
                    date = date,
                    isToday = date == state.today,
                    formatters = formatters,
                    onClick = { state.eventSink(CalendarEvent.SelectDate(date)) },
                )
                repeat(allDayRows) { row ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AllDayChipHeight + 2.dp)
                            .padding(horizontal = 1.dp, vertical = 1.dp),
                    ) {
                        allDayEntries.getOrNull(row)?.let { CalendarAllDayChip(it) }
                    }
                }
                if (allDayRows > 0) Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CalendarDayHeader(
    date: LocalDate,
    isToday: Boolean,
    formatters: CalendarFormatters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = formatters.weekdayShort(date),
            style = ElementTheme.typography.fontBodySmMedium,
            color = if (isToday) ElementTheme.colors.textActionAccent else ElementTheme.colors.textSecondary,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(DayHeaderCircleSize)
                .then(if (isToday) Modifier.background(ElementTheme.colors.bgActionPrimaryRest, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatters.dayOfMonth(date),
                style = if (isToday) ElementTheme.typography.fontBodyLgMedium else ElementTheme.typography.fontBodyLgRegular,
                color = if (isToday) ElementTheme.colors.textOnSolidPrimary else ElementTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun CalendarAllDayChip(
    entry: CalendarEntry,
    modifier: Modifier = Modifier,
) {
    val style = calendarEntryStyle(entry)
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(style.container)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = calendarEntryTitle(entry),
            style = ElementTheme.typography.fontBodyXsMedium,
            color = style.title,
            textDecoration = style.titleDecoration,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CalendarHourGutter(
    formatters: CalendarFormatters,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        // The first line is the top of the grid, it does not need a label.
        for (hour in 1..23) {
            Text(
                text = formatters.hour(hour),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    // Center the label, which is about 16dp high, on its hour line.
                    .offset(y = CalendarHourHeight * hour - 8.dp)
                    .padding(end = 8.dp),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CalendarGridPage(
    state: CalendarState,
    formatters: CalendarFormatters,
    firstDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val days = state.viewMode.days
    Row(modifier.fillMaxSize()) {
        repeat(days) { offset ->
            CalendarDayColumn(
                date = firstDate.plusDays(offset.toLong()),
                state = state,
                formatters = formatters,
                showDetails = days <= MAX_DAYS_WITH_ENTRY_DETAILS,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun CalendarDayColumn(
    date: LocalDate,
    state: CalendarState,
    formatters: CalendarFormatters,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val lineColor = ElementTheme.colors.separatorSecondary
    val positionedEntries = remember(state.entries, date) { layoutEntriesForDay(state.entries, date) }
    val nowMinute = nowIndicatorMinute(state.now, date)
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier
            // Tapping an empty slot starts an event there, the way a desktop calendar does.
            .pointerInput(date, density) {
                detectTapGestures { offset ->
                    val minute = with(density) { offset.y.toDp() } / CalendarHourHeight * 60f
                    state.eventSink(CalendarEvent.NewEventAt(date.atTime(slotTimeOf(minute))))
                }
            }
            .drawBehind {
            val strokeWidth = 1.dp.toPx()
            val hourHeightPx = CalendarHourHeight.toPx()
            for (hour in 1..23) {
                val y = hour * hourHeightPx
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
            }
            // The divider separates the column from the hour labels, which are on the trailing side in RTL.
            val dividerX = if (layoutDirection == LayoutDirection.Rtl) size.width else 0f
            drawLine(lineColor, Offset(dividerX, 0f), Offset(dividerX, size.height), strokeWidth)
        },
    ) {
        positionedEntries.forEach { positioned ->
            val slotWidth = maxWidth / positioned.columnCount
            val height = CalendarHourHeight * ((positioned.endMinute - positioned.startMinute) / 60f)
            CalendarEntryCard(
                entry = positioned.entry,
                timeRange = formatters.timeRange(positioned.entry),
                compact = true,
                showDetails = showDetails && height >= MinHeightForEntryDetails,
                modifier = Modifier
                    .offset(x = slotWidth * positioned.column, y = CalendarHourHeight * (positioned.startMinute / 60f))
                    .width(slotWidth)
                    .height(height)
                    .padding(1.dp)
                    .clickable { state.eventSink(CalendarEvent.OpenEntry(positioned.entry)) },
            )
        }
        if (nowMinute != null) {
            CalendarNowIndicator(
                modifier = Modifier.offset(y = CalendarHourHeight * (nowMinute / 60f) - 4.dp),
            )
        }
    }
}

@Composable
private fun CalendarNowIndicator(
    modifier: Modifier = Modifier,
) {
    val color = ElementTheme.colors.bgCriticalPrimary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Box(
            Modifier
                .weight(1f)
                .height(2.dp)
                .background(color)
        )
    }
}

/**
 * The start of the half hour slot a tap at [minuteOfDay] falls in, which is how calendars turn a tap on the grid
 * into a sensible start time. The last slot of the day is kept in range so a tap near midnight still works.
 */
private fun slotTimeOf(minuteOfDay: Float): LocalTime {
    val slot = (minuteOfDay / SLOT_MINUTES).toInt().coerceIn(0, SLOTS_PER_DAY - 1)
    return LocalTime.MIDNIGHT.plusMinutes(slot * SLOT_MINUTES.toLong())
}

private const val SLOT_MINUTES = 30
private const val SLOTS_PER_DAY = 24 * 60 / SLOT_MINUTES
