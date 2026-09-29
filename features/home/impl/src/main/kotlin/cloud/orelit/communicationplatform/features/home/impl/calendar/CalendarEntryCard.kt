/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

/**
 * A calendar entry card: a tinted container with a solid accent bar on its leading edge.
 * The [compact] style is the dense one meant for time grid blocks, and [showDetails] adds
 * the time range and the location, when there is enough room for them.
 *
 * A cancelled entry keeps its place but is greyed out and struck through, the way Teams shows it.
 */
@Composable
internal fun CalendarEntryCard(
    entry: CalendarEntry,
    timeRange: String,
    compact: Boolean,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val style = calendarEntryStyle(entry)
    val shape = RoundedCornerShape(if (compact) 6.dp else 12.dp)
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(style.container)
            .semantics(mergeDescendants = true) {},
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(style.accent)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = if (compact) 6.dp else 12.dp,
                    vertical = if (compact) 3.dp else 10.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = calendarEntryTitle(entry),
                    modifier = Modifier.weight(1f, fill = false),
                    style = if (compact) ElementTheme.typography.fontBodySmMedium else ElementTheme.typography.fontBodyLgMedium,
                    color = style.title,
                    textDecoration = style.titleDecoration,
                    maxLines = if (compact && !showDetails) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (entry.isOnlineMeeting) {
                    Icon(
                        imageVector = CompoundIcons.VideoCallSolid(),
                        contentDescription = stringResource(R.string.screen_calendar_online_meeting),
                        tint = style.icon,
                        modifier = Modifier.size(if (compact) 14.dp else 18.dp),
                    )
                }
            }
            if (showDetails) {
                Text(
                    text = timeRange,
                    style = if (compact) ElementTheme.typography.fontBodyXsRegular else ElementTheme.typography.fontBodyMdRegular,
                    color = style.details,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val location = entry.location
                if (location != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = CompoundIcons.LocationPin(),
                            contentDescription = null,
                            tint = style.detailsIcon,
                            modifier = Modifier.size(if (compact) 12.dp else 16.dp),
                        )
                        Text(
                            text = location,
                            style = if (compact) ElementTheme.typography.fontBodyXsRegular else ElementTheme.typography.fontBodyMdRegular,
                            color = style.details,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** The colours of an entry on the calendar, greyed out once it is cancelled. */
internal data class CalendarEntryStyle(
    val container: Color,
    val accent: Color,
    val title: Color,
    val titleDecoration: TextDecoration?,
    val details: Color,
    val icon: Color,
    val detailsIcon: Color,
)

@Composable
internal fun calendarEntryStyle(entry: CalendarEntry): CalendarEntryStyle {
    val accent = calendarAccent(entry.accentIndex)
    return if (entry.isCancelled) {
        CalendarEntryStyle(
            container = ElementTheme.colors.bgSubtleSecondary,
            accent = ElementTheme.colors.iconDisabled,
            title = ElementTheme.colors.textDisabled,
            titleDecoration = TextDecoration.LineThrough,
            details = ElementTheme.colors.textDisabled,
            icon = ElementTheme.colors.iconDisabled,
            detailsIcon = ElementTheme.colors.iconDisabled,
        )
    } else {
        CalendarEntryStyle(
            container = accent.container,
            accent = accent.content,
            title = accent.content,
            titleDecoration = null,
            details = ElementTheme.colors.textSecondary,
            icon = accent.content,
            detailsIcon = ElementTheme.colors.iconSecondary,
        )
    }
}

/** The title of an entry as the calendar shows it, which says so when the entry is cancelled. */
@Composable
internal fun calendarEntryTitle(entry: CalendarEntry): String {
    return if (entry.isCancelled) stringResource(R.string.screen_calendar_event_canceled_title, entry.title) else entry.title
}

@PreviewsDayNight
@Composable
internal fun CalendarEntryCardPreview(
    @PreviewParameter(CalendarEntryPreviewParam::class) entry: CalendarEntry,
) = ElementPreview {
    val formatters = rememberCalendarFormatters()
    CalendarEntryCard(
        entry = entry,
        timeRange = formatters.timeRange(entry),
        compact = false,
        showDetails = true,
    )
}
