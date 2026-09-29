/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.impl.direct

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutEvent
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutState
import cloud.orelit.communicationplatform.features.logout.impl.tools.isBackingUp
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.architecture.runCatchingUpdatingState
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.BackupUploadState
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class DirectLogoutPresenter(
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
) : Presenter<DirectLogoutState> {
    @Composable
    override fun present(): DirectLogoutState {
        val localCoroutineScope = rememberCoroutineScope()

        val logoutAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val backupUploadState: BackupUploadState by remember {
            encryptionService.waitForBackupUploadSteadyState()
        }
            .collectAsState(initial = BackupUploadState.Unknown)

        val isLastDevice by encryptionService.isLastDevice.collectAsState()

        fun handleEvent(event: DirectLogoutEvent) {
            when (event) {
                is DirectLogoutEvent.Logout -> {
                    if (logoutAction.value.isConfirming() || event.ignoreSdkError) {
                        localCoroutineScope.logout(logoutAction, event.ignoreSdkError)
                    } else {
                        logoutAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                DirectLogoutEvent.CloseDialogs -> {
                    logoutAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return DirectLogoutState(
            canDoDirectSignOut = !isLastDevice &&
                !backupUploadState.isBackingUp(),
            logoutAction = logoutAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.logout(
        logoutAction: MutableState<AsyncAction<Unit>>,
        ignoreSdkError: Boolean,
    ) = launch {
        suspend {
            matrixClient.logout(userInitiated = true, ignoreSdkError)
        }.runCatchingUpdatingState(logoutAction)
    }
}
