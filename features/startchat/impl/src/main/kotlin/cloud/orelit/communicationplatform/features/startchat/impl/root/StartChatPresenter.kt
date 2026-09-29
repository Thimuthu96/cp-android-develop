/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.startchat.api.StartDMAction
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.SelectionMode
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListDataStore
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListPresenter
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListPresenterArgs
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserRepository
import kotlinx.coroutines.launch

@Inject
class StartChatPresenter(
    presenterFactory: UserListPresenter.Factory,
    userRepository: UserRepository,
    userListDataStore: UserListDataStore,
    private val startDMAction: StartDMAction,
    private val buildMeta: BuildMeta,
) : Presenter<StartChatState> {
    private val presenter = presenterFactory.create(
        UserListPresenterArgs(
            selectionMode = SelectionMode.Single,
        ),
        userRepository,
        userListDataStore,
    )

    @Composable
    override fun present(): StartChatState {
        val userListState = presenter.present()

        val localCoroutineScope = rememberCoroutineScope()
        val startDmActionState: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvent(event: StartChatEvent) {
            when (event) {
                is StartChatEvent.StartDM -> localCoroutineScope.launch {
                    startDMAction.execute(
                        matrixUser = event.matrixUser,
                        createIfDmDoesNotExist = startDmActionState.value is AsyncAction.Confirming,
                        actionState = startDmActionState,
                    )
                }
                StartChatEvent.CancelStartDM -> startDmActionState.value = AsyncAction.Uninitialized
            }
        }

        return StartChatState(
            applicationName = buildMeta.applicationName,
            userListState = userListState,
            startDmAction = startDmActionState.value,
            eventSink = ::handleEvent,
        )
    }
}
