/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package cloud.orelit.communicationplatform.features.home.impl.calendar.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.calendar.calendarAccent
import cloud.orelit.communicationplatform.features.home.impl.calendar.recurrenceLabelRes
import cloud.orelit.communicationplatform.features.home.impl.calendar.rememberCalendarFormatters
import cloud.orelit.communicationplatform.libraries.androidutils.system.copyToClipboard
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ListOption
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.SingleSelectionDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ModalBottomSheet
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.OutlinedButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import kotlinx.collections.immutable.persistentListOf

/**
 * The details of an event, in a bottom sheet the way Teams and Outlook show them: what and when at the top,
 * then who is invited and what the event is about, with joining the call and opening its chat always in reach.
 *
 * Opening the chat is a navigation, which the presenter cannot do, so it is handled by [onOpenChat] while the
 * other actions go through the state's event sink.
 *
 * The organiser also gets Edit next to the title and Cancel event at the bottom, the way Teams offers them. A
 * cancelled event says so in a banner at the top and can no longer be joined, while its chat and link keep working.
 */
@Composable
fun CalendarEventDetailsView(
    state: CalendarEventDetailsState,
    onOpenChat: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = state.entry
    val formatters = rememberCalendarFormatters()
    val accent = calendarAccent(entry.accentIndex)
    val eventSink = state.eventSink

    ModalBottomSheet(
        onDismissRequest = { eventSink(CalendarEventDetailsEvent.Dismiss) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        scrollable = true,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.isCancelled) {
                CalendarEventCancelledBanner()
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // The same colour the entry has on the grid, so the sheet is clearly about the card just tapped.
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (state.isCancelled) ElementTheme.colors.iconDisabled else accent.content)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = entry.title.ifBlank { stringResource(R.string.screen_calendar_untitled_event) },
                        style = ElementTheme.typography.fontHeadingSmMedium,
                        color = if (state.isCancelled) ElementTheme.colors.textDisabled else ElementTheme.colors.textPrimary,
                        textDecoration = if (state.isCancelled) TextDecoration.LineThrough else null,
                    )
                    Text(
                        text = formatters.detailWhen(entry, stringResource(R.string.screen_calendar_all_day)),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                    if (state.hasActiveCall) {
                        Text(
                            text = stringResource(R.string.screen_calendar_event_call_in_progress),
                            style = ElementTheme.typography.fontBodySmMedium,
                            color = ElementTheme.colors.textSuccessPrimary,
                        )
                    }
                }
                if (state.showManageActions) {
                    IconButton(
                        onClick = { eventSink(CalendarEventDetailsEvent.Edit) },
                    ) {
                        Icon(
                            imageVector = CompoundIcons.Edit(),
                            contentDescription = stringResource(R.string.screen_calendar_edit_event),
                            tint = ElementTheme.colors.iconSecondary,
                        )
                    }
                }
            }

            CalendarEventActions(state = state, onOpenChat = onOpenChat)

            HorizontalDivider()

            entry.rrule?.let { rrule ->
                CalendarEventDetailsRow(
                    icon = CompoundIcons.Restart(),
                    text = stringResource(recurrenceLabelRes(rrule)),
                )
            }
            entry.location?.let { location ->
                CalendarEventDetailsRow(icon = CompoundIcons.LocationPin(), text = location)
            }
            state.organizer?.let { organizer ->
                CalendarEventPerson(
                    user = organizer,
                    caption = stringResource(R.string.screen_calendar_event_organizer),
                )
            }
            if (state.participantCount > 0) {
                CalendarEventParticipants(state = state)
            }
            entry.description?.takeIf { it.isNotBlank() }?.let { description ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.screen_calendar_event_description),
                        style = ElementTheme.typography.fontBodySmMedium,
                        color = ElementTheme.colors.textSecondary,
                    )
                    Text(
                        text = description,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textPrimary,
                    )
                }
            }

            if (state.showManageActions) {
                HorizontalDivider()
                TextButton(
                    text = stringResource(R.string.screen_calendar_cancel_event),
                    onClick = { eventSink(CalendarEventDetailsEvent.Cancel) },
                    destructive = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    state.pendingAction?.let { pendingAction ->
        CalendarEventManageDialog(pendingAction = pendingAction, eventSink = eventSink)
    }
}

/** Says the event was called off, before anything else in the sheet. */
@Composable
private fun CalendarEventCancelledBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(ElementTheme.colors.bgCriticalSubtle)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CompoundIcons.Close(),
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.screen_calendar_event_canceled_banner),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textCriticalPrimary,
        )
    }
}

/** Asks which occurrences a change is for, then confirms a cancellation, which cannot be undone. */
@Composable
private fun CalendarEventManageDialog(
    pendingAction: ManageAction,
    eventSink: (CalendarEventDetailsEvent) -> Unit,
) {
    when (pendingAction) {
        is ManageAction.AskScope -> {
            // In the order of the options below.
            val scopes = listOf(EventScope.Occurrence, EventScope.Series)
            val options = persistentListOf(
                ListOption(stringResource(R.string.screen_calendar_scope_occurrence)),
                ListOption(stringResource(R.string.screen_calendar_scope_series)),
            )
            SingleSelectionDialog(
                title = stringResource(
                    when (pendingAction.kind) {
                        ManageAction.Kind.Edit -> R.string.screen_calendar_scope_edit_title
                        ManageAction.Kind.Cancel -> R.string.screen_calendar_scope_cancel_title
                    }
                ),
                options = options,
                onSelectOption = { index -> eventSink(CalendarEventDetailsEvent.ChooseScope(scopes[index])) },
                onDismissRequest = { eventSink(CalendarEventDetailsEvent.DismissDialog) },
            )
        }
        is ManageAction.ConfirmCancel -> {
            val isOccurrence = pendingAction.scope == EventScope.Occurrence
            ConfirmationDialog(
                title = stringResource(if (isOccurrence) R.string.screen_calendar_cancel_occurrence_title else R.string.screen_calendar_cancel_event_title),
                content = stringResource(
                    if (isOccurrence) R.string.screen_calendar_cancel_occurrence_message else R.string.screen_calendar_cancel_event_message
                ),
                submitText = stringResource(if (isOccurrence) R.string.screen_calendar_cancel_occurrence else R.string.screen_calendar_cancel_event),
                cancelText = stringResource(R.string.screen_calendar_keep_event),
                destructiveSubmit = true,
                onSubmitClick = { eventSink(CalendarEventDetailsEvent.ConfirmCancel) },
                onDismiss = { eventSink(CalendarEventDetailsEvent.DismissDialog) },
            )
        }
    }
}

/** Join, chat and copy link: everything a participant needs to actually attend the meeting. */
@Composable
private fun CalendarEventActions(
    state: CalendarEventDetailsState,
    onOpenChat: (RoomId) -> Unit,
) {
    val eventSink = state.eventSink
    val roomId = state.roomId
    val context = LocalContext.current
    val meetingLink = state.meetingLink
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            text = stringResource(R.string.screen_calendar_event_join_call),
            onClick = { eventSink(CalendarEventDetailsEvent.JoinCall) },
            enabled = state.canJoinCall,
            leadingIcon = IconSource.Vector(CompoundIcons.VideoCallSolid()),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                text = stringResource(R.string.screen_calendar_event_chat),
                onClick = { roomId?.let(onOpenChat) },
                enabled = roomId != null,
                leadingIcon = IconSource.Vector(CompoundIcons.Chat()),
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                text = stringResource(R.string.screen_calendar_event_copy_link),
                onClick = {
                    meetingLink?.let {
                        context.copyToClipboard(it)
                        eventSink(CalendarEventDetailsEvent.CopyLink)
                    }
                },
                // Stays disabled until the link has been resolved, so a tap always copies something.
                enabled = meetingLink != null,
                leadingIcon = IconSource.Vector(CompoundIcons.Link()),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarEventDetailsRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun CalendarEventPerson(
    user: MatrixUser,
    caption: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = user.getAvatarData(AvatarSize.UserListItem),
            avatarType = AvatarType.User,
        )
        Column {
            Text(
                text = user.displayName?.takeIf { it.isNotBlank() } ?: user.userId.value,
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = caption,
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun CalendarEventParticipants(state: CalendarEventDetailsState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = pluralStringResource(
                R.plurals.screen_calendar_event_participants,
                state.participantCount.toInt(),
                state.participantCount.toInt(),
            ),
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            // Only the members the room list knows about are shown; the count above is the authoritative total.
            state.participants.take(MAX_SHOWN_PARTICIPANTS).forEach { participant ->
                Avatar(
                    avatarData = participant.getAvatarData(AvatarSize.TimelineReadReceipt),
                    avatarType = AvatarType.User,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElementTheme.colors.bgCanvasDefault)
                        .padding(2.dp),
                )
            }
        }
    }
}

private const val MAX_SHOWN_PARTICIPANTS = 8

@PreviewsDayNight
@Composable
internal fun CalendarEventDetailsViewPreview(
    @PreviewParameter(CalendarEventDetailsStatePreviewParam::class) state: CalendarEventDetailsState,
) = ElementPreview {
    Box {
        Spacer(modifier = Modifier.height(320.dp))
        CalendarEventDetailsView(state = state, onOpenChat = {})
    }
}
