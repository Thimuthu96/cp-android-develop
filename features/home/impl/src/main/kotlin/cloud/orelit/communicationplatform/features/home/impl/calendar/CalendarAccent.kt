/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import cloud.orelit.communicationplatform.compound.theme.ElementTheme

/** Colors of an entry, taken from the decorative palette of the design system. */
@Immutable
internal data class CalendarAccent(
    val container: Color,
    val content: Color,
)

@Composable
internal fun calendarAccent(index: Int): CalendarAccent {
    val colors = ElementTheme.colors
    return when (Math.floorMod(index, 6)) {
        0 -> CalendarAccent(colors.bgDecorative1, colors.textDecorative1)
        1 -> CalendarAccent(colors.bgDecorative2, colors.textDecorative2)
        2 -> CalendarAccent(colors.bgDecorative3, colors.textDecorative3)
        3 -> CalendarAccent(colors.bgDecorative4, colors.textDecorative4)
        4 -> CalendarAccent(colors.bgDecorative5, colors.textDecorative5)
        else -> CalendarAccent(colors.bgDecorative6, colors.textDecorative6)
    }
}
