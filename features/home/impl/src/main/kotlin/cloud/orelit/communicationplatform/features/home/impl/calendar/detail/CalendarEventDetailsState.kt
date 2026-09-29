/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.detail

import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEntry
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

/**
 * The details of the event the user tapped, shown in a bottom sheet.
 */
data class CalendarEventDetailsState(
    val entry: CalendarEntry,
    /** The organiser with their profile resolved, null while it is still loading or for an event without a room. */
    val organizer: MatrixUser?,
    /** The participants known from the room, which can be fewer than [participantCount]. */
    val participants: ImmutableList<MatrixUser>,
    val participantCount: Long,
    /** True while a call is running in the event's room, so the sheet can say the meeting has started. */
    val hasActiveCall: Boolean,
    /** The shareable link to the meeting, null until it has been resolved or for an event without a room. */
    val meetingLink: String?,
    /** True when the user organised this event, so they may edit it or call it off. */
    val canManage: Boolean = false,
    /** The dialog the edit or cancel flow is waiting on, null when none is shown. */
    val pendingAction: ManageAction? = null,
    val eventSink: (CalendarEventDetailsEvent) -> Unit,
) {
    /** The room of the event, null for an entry that has none, such as the sample data. */
    val roomId: RoomId? get() = entry.room?.roomId

    /** Calling, chatting and linking all need a room, so they are offered only for an event that has one. */
    val hasRoom: Boolean get() = roomId != null

    val isCancelled: Boolean get() = entry.isCancelled

    /** A cancelled event cannot be changed any more, not even by its organiser. */
    val showManageActions: Boolean get() = canManage && hasRoom && !isCancelled

    /** A call cannot be joined once the event is called off, while its chat and link keep working. */
    val canJoinCall: Boolean get() = hasRoom && !isCancelled
}

/** Whether a change applies to the occurrence the user opened, or to the whole series. */
enum class EventScope {
    Occurrence,
    Series,
}

/** What the organiser is asked while editing or cancelling. */
sealed interface ManageAction {
    /** Which occurrences to [kind] a recurring event for. */
    data class AskScope(val kind: Kind) : ManageAction

    /** The last chance to keep the event before it is cancelled for everyone. */
    data class ConfirmCancel(val scope: EventScope) : ManageAction

    enum class Kind {
        Edit,
        Cancel,
    }
}

sealed interface CalendarEventDetailsEvent {
    /** Joins the call of the event, starting it when nobody has yet. */
    data object JoinCall : CalendarEventDetailsEvent

    /** Tells the presenter the link was copied, so it can confirm it to the user. */
    data object CopyLink : CalendarEventDetailsEvent
    data object Dismiss : CalendarEventDetailsEvent

    /** Starts editing the event, asking which occurrences first for a recurring one. */
    data object Edit : CalendarEventDetailsEvent

    /** Starts cancelling the event, asking which occurrences first for a recurring one. */
    data object Cancel : CalendarEventDetailsEvent

    /** Answers the question of [ManageAction.AskScope]. */
    data class ChooseScope(val scope: EventScope) : CalendarEventDetailsEvent

    /** Answers yes to [ManageAction.ConfirmCancel]. */
    data object ConfirmCancel : CalendarEventDetailsEvent

    /** Closes the dialog of [CalendarEventDetailsState.pendingAction] without doing anything. */
    data object DismissDialog : CalendarEventDetailsEvent
}
