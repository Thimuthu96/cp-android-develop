/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A swipeable strip displaying the days of a week, used to pick the selected date. */
@Composable
internal fun CalendarWeekStrip(
    state: CalendarState,
    formatters: CalendarFormatters,
    modifier: Modifier = Modifier,
) {
    val anchor = remember { state.today.startOfWeek(state.firstDayOfWeek) }
    val pagerState = rememberDatePagerState(
        selectedDate = state.selectedDate,
        anchor = anchor,
        step = 7,
    ) { firstDate ->
        // Keep the same day of the week when swiping to another week.
        val offset = ChronoUnit.DAYS.between(state.selectedDate.startOfWeek(state.firstDayOfWeek), state.selectedDate)
        state.eventSink(CalendarEvent.SelectDate(firstDate.plusDays(offset)))
    }
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
    ) { page ->
        val weekStart = pageToStartDate(page, anchor, 7)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            repeat(7) { offset ->
                val date = weekStart.plusDays(offset.toLong())
                CalendarStripDay(
                    date = date,
                    isSelected = date == state.selectedDate,
                    isToday = date == state.today,
                    hasEntries = state.entries.any { it.occursOn(date) },
                    formatters = formatters,
                    onClick = { state.eventSink(CalendarEvent.SelectDate(date)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CalendarStripDay(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEntries: Boolean,
    formatters: CalendarFormatters,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = formatters.weekdayNarrow(date),
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .then(
                    when {
                        isSelected -> Modifier.background(ElementTheme.colors.bgActionPrimaryRest, CircleShape)
                        isToday -> Modifier.border(1.dp, ElementTheme.colors.borderInteractivePrimary, CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = formatters.dayOfMonth(date),
                style = if (isSelected || isToday) {
                    ElementTheme.typography.fontBodyLgMedium
                } else {
                    ElementTheme.typography.fontBodyLgRegular
                },
                color = when {
                    isSelected -> ElementTheme.colors.textOnSolidPrimary
                    isToday -> ElementTheme.colors.textActionAccent
                    else -> ElementTheme.colors.textPrimary
                },
            )
        }
        // Reserve the space so the strip does not jump when a day has entries.
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(
                    color = if (hasEntries) ElementTheme.colors.iconTertiary else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun CalendarWeekStripPreview(
    @PreviewParameter(CalendarStatePreviewParam::class) state: CalendarState,
) = ElementPreview {
    CalendarWeekStrip(
        state = state,
        formatters = rememberCalendarFormatters(),
    )
}
