/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.DropdownMenu
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.DropdownMenuItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

/** The actions of the top bar when the calendar tab is selected: go to today and switch the view mode. */
@Composable
internal fun RowScope.CalendarTopBarActions(
    state: CalendarState,
) {
    val formatters = rememberCalendarFormatters()
    IconButton(onClick = { state.eventSink(CalendarEvent.GoToToday) }) {
        CalendarTodayIcon(dayOfMonth = formatters.dayOfMonth(state.today))
    }
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = if (state.viewMode == CalendarViewMode.Agenda) CompoundIcons.ListBulleted() else CompoundIcons.Grid(),
                contentDescription = stringResource(R.string.screen_calendar_change_view),
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            CalendarViewMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(stringResource(mode.labelRes)) },
                    onClick = {
                        showMenu = false
                        state.eventSink(CalendarEvent.SelectViewMode(mode))
                    },
                    trailingIcon = if (mode == state.viewMode) {
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

/** A calendar page icon showing the current day of the month. */
@Composable
private fun CalendarTodayIcon(
    dayOfMonth: String,
    modifier: Modifier = Modifier,
) {
    val tint = ElementTheme.colors.iconPrimary
    val description = stringResource(R.string.screen_calendar_today)
    Box(
        modifier = modifier
            .size(24.dp)
            .border(width = 2.dp, color = tint, shape = RoundedCornerShape(6.dp))
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = dayOfMonth,
            style = ElementTheme.typography.fontBodyXsMedium,
            color = tint,
            maxLines = 1,
        )
    }
}
