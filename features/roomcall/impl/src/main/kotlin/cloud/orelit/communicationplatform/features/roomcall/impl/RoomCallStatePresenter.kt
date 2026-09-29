/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomcall.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.call.api.CurrentCall
import cloud.orelit.communicationplatform.features.call.api.CurrentCallService
import cloud.orelit.communicationplatform.features.enterprise.api.SessionEnterpriseService
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CallIntentConsensus
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.canCall
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.permissionsAsState

@Inject
class RoomCallStatePresenter(
    private val room: JoinedRoom,
    private val currentCallService: CurrentCallService,
    private val sessionEnterpriseService: SessionEnterpriseService,
) : Presenter<RoomCallState> {
    @Composable
    override fun present(): RoomCallState {
        val isAvailable by produceState(false) {
            value = sessionEnterpriseService.isElementCallAvailable()
        }
        val roomInfo by room.roomInfoFlow.collectAsState()
        val canJoinCall by room.permissionsAsState(false) { perms -> perms.canCall() }
        val isUserInTheCall by remember {
            derivedStateOf {
                room.sessionId in roomInfo.activeRoomCallParticipants
            }
        }
        val currentCall by currentCallService.currentCall.collectAsState()
        val isUserLocallyInTheCall by remember {
            derivedStateOf {
                (currentCall as? CurrentCall.RoomCall)?.roomId == room.roomId
            }
        }
        val callState by remember {
            derivedStateOf {
                when {
                    isAvailable.not() -> RoomCallState.Unavailable
                    roomInfo.hasRoomCall -> RoomCallState.OnGoing(
                        canJoinCall = canJoinCall,
                        isUserInTheCall = isUserInTheCall,
                        isUserLocallyInTheCall = isUserLocallyInTheCall,
                        isAudioCall = roomInfo.activeCallIntentConsensus.isAudio(),
                    )
                    else -> {
                        val isEmptyDm = roomInfo.isDm && roomInfo.activeMembersCount <= 1
                        RoomCallState.StandBy(
                            canStartCall = canJoinCall && !isEmptyDm,
                            isDM = roomInfo.isDm
                        )
                    }
                }
            }
        }
        return callState
    }
}

fun CallIntentConsensus.isAudio(): Boolean {
    val intent = when (this) {
        is CallIntentConsensus.Full -> callIntent
        is CallIntentConsensus.Partial -> callIntent
        is CallIntentConsensus.None -> return false
    }
    return intent == CallIntent.AUDIO
}
