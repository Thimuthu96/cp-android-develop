/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipObserver
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MembershipChange
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Inject
class LoggedInEventProcessor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val roomMembershipObserver: RoomMembershipObserver,
) {
    private var observingJob: Job? = null

    fun observeEvents(coroutineScope: CoroutineScope) {
        observingJob = roomMembershipObserver.updates
            .filter { !it.isUserInRoom }
            .distinctUntilChanged()
            .onEach { roomMemberShipUpdate ->
                when (roomMemberShipUpdate.change) {
                    MembershipChange.LEFT -> {
                        displayMessage(
                            if (roomMemberShipUpdate.isSpace) {
                                CommonStrings.common_current_user_left_space
                            } else {
                                CommonStrings.common_current_user_left_room
                            }
                        )
                    }
                    MembershipChange.INVITATION_REJECTED -> displayMessage(CommonStrings.common_current_user_rejected_invite)
                    MembershipChange.KNOCK_RETRACTED -> displayMessage(CommonStrings.common_current_user_canceled_knock)
                    else -> Unit
                }
            }
            .launchIn(coroutineScope)
    }

    fun stopObserving() {
        observingJob?.cancel()
        observingJob = null
    }

    private fun displayMessage(message: Int) {
        snackbarDispatcher.post(SnackbarMessage(message))
    }
}
