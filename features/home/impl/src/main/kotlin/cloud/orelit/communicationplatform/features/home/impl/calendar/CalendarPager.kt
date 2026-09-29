/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import java.time.LocalDate

/**
 * Creates a [PagerState] where each page displays [step] days, and keeps it in sync with [selectedDate].
 * When [selectedDate] changes to a date displayed by another page, the pager scrolls to that page.
 * When the user settles on a page which does not display [selectedDate], [onSettle] is invoked with the
 * first date of that page. Checking the displayed page is what prevents a feedback loop between the two directions.
 *
 * The [anchor], the first date of the initial page, must remain the same for the lifetime of the pager.
 */
@Composable
internal fun rememberDatePagerState(
    selectedDate: LocalDate,
    anchor: LocalDate,
    step: Int,
    onSettle: (firstDate: LocalDate) -> Unit,
): PagerState {
    val pagerState = rememberPagerState(
        initialPage = dateToPage(selectedDate, anchor, step),
        pageCount = { CALENDAR_PAGE_COUNT },
    )
    val latestSelectedDate by rememberUpdatedState(selectedDate)
    val latestOnPageSettled by rememberUpdatedState(onSettle)

    LaunchedEffect(pagerState, anchor, step) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (dateToPage(latestSelectedDate, anchor, step) != page) {
                latestOnPageSettled(pageToStartDate(page, anchor, step))
            }
        }
    }
    LaunchedEffect(pagerState, anchor, step, selectedDate) {
        val target = dateToPage(selectedDate, anchor, step)
        if (pagerState.settledPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }
    return pagerState
}
