/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.reset.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.architecture.runCatchingUpdatingState
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ResetIdentityPasswordPresenter(
    private val identityPasswordResetHandle: IdentityPasswordResetHandle,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<ResetIdentityPasswordState> {
    @Composable
    override fun present(): ResetIdentityPasswordState {
        val coroutineScope = rememberCoroutineScope()

        val resetAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvent(event: ResetIdentityPasswordEvent) {
            when (event) {
                is ResetIdentityPasswordEvent.Reset -> coroutineScope.reset(event.password, resetAction)
                ResetIdentityPasswordEvent.DismissError -> resetAction.value = AsyncAction.Uninitialized
            }
        }

        return ResetIdentityPasswordState(
            resetAction = resetAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.reset(password: String, action: MutableState<AsyncAction<Unit>>) = launch(dispatchers.io) {
        suspend {
            identityPasswordResetHandle.resetPassword(password).getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
