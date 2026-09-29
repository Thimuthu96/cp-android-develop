/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight

/** The first hour shown in a time grid when it does not display the current time. */
private const val DEFAULT_FIRST_VISIBLE_HOUR = 8

/**
 * The content of the calendar tab. The top bar (month title, today and view mode actions)
 * is rendered by the home top bar. The bottom of [contentPadding] keeps the content clear
 * of the floating navigation bar.
 */
@Composable
fun CalendarView(
    state: CalendarState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val formatters = rememberCalendarFormatters()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    // Shared by the time grid modes, so the scroll position is kept when switching between them.
    val firstVisibleHour = if (state.selectedDate == state.today) {
        (state.now.hour - 1).coerceAtLeast(0)
    } else {
        DEFAULT_FIRST_VISIBLE_HOUR
    }
    val scrollState = rememberScrollState(initial = with(density) { (CalendarHourHeight * firstVisibleHour).roundToPx() })

    Box(
        modifier = modifier.padding(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
        ),
    ) {
        // Each mode has its own pager or list state.
        key(state.viewMode) {
            if (state.viewMode.isTimeGrid) {
                CalendarTimeGridView(
                    state = state,
                    formatters = formatters,
                    scrollState = scrollState,
                    bottomPadding = contentPadding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                CalendarAgendaView(
                    state = state,
                    formatters = formatters,
                    bottomPadding = contentPadding.calculateBottomPadding(),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun CalendarViewPreview(
    @PreviewParameter(CalendarStatePreviewParam::class) state: CalendarState,
) = ElementPreview {
    CalendarView(
        state = state,
        contentPadding = PaddingValues(),
        modifier = Modifier.height(640.dp),
    )
}
