/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailureFactory
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.architecture.runUpdatingState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.coroutines.launch

@Inject
class ResolveVerifiedUserSendFailurePresenter(
    private val room: JoinedRoom,
    private val verifiedUserSendFailureFactory: VerifiedUserSendFailureFactory,
) : Presenter<ResolveVerifiedUserSendFailureState> {
    @Composable
    override fun present(): ResolveVerifiedUserSendFailureState {
        var resolver by remember {
            mutableStateOf<VerifiedUserSendFailureResolver?>(null)
        }
        val verifiedUserSendFailure by produceState<VerifiedUserSendFailure>(VerifiedUserSendFailure.None, resolver?.currentSendFailure?.value) {
            val currentSendFailure = resolver?.currentSendFailure?.value
            value = verifiedUserSendFailureFactory.create(currentSendFailure)
        }

        val resolveAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val retryAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val coroutineScope = rememberCoroutineScope()

        fun handleEvent(event: ResolveVerifiedUserSendFailureEvent) {
            when (event) {
                is ResolveVerifiedUserSendFailureEvent.ComputeForMessage -> {
                    val sendState = event.messageEvent.localSendState as? LocalEventSendState.Failed.VerifiedUser
                    val transactionId = event.messageEvent.transactionId
                    val sendHandle = event.messageEvent.sendhandle
                    resolver = if (sendState != null && transactionId != null && sendHandle != null) {
                        VerifiedUserSendFailureResolver(
                            room = room,
                            transactionId = transactionId,
                            sendHandle = sendHandle,
                            iterator = VerifiedUserSendFailureIterator.from(sendState)
                        )
                    } else {
                        null
                    }
                }
                ResolveVerifiedUserSendFailureEvent.Dismiss -> {
                    resolver = null
                }
                ResolveVerifiedUserSendFailureEvent.Retry -> {
                    coroutineScope.launch {
                        resolver?.run {
                            runUpdatingState(retryAction) {
                                resend()
                            }
                        }
                    }
                }
                ResolveVerifiedUserSendFailureEvent.ResolveAndResend -> {
                    coroutineScope.launch {
                        resolver?.run {
                            runUpdatingState(resolveAction) {
                                resolveAndResend()
                            }
                        }
                    }
                }
            }
        }

        return ResolveVerifiedUserSendFailureState(
            verifiedUserSendFailure = verifiedUserSendFailure,
            resolveAction = resolveAction.value,
            retryAction = retryAction.value,
            eventSink = ::handleEvent,
        )
    }
}
