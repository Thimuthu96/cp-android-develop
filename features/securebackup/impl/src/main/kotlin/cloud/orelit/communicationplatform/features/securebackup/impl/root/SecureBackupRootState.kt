/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.root

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.BackupState
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.RecoveryState

data class SecureBackupRootState(
    val enableAction: AsyncAction<Unit>,
    val backupState: BackupState,
    val doesBackupExistOnServer: AsyncData<Boolean>,
    val recoveryState: RecoveryState,
    val appName: String,
    val displayKeyStorageDisabledError: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (SecureBackupRootEvent) -> Unit,
) {
    val isKeyStorageEnabled: Boolean
        get() = when (backupState) {
            BackupState.UNKNOWN -> doesBackupExistOnServer.dataOrNull() == true
            BackupState.CREATING,
            BackupState.ENABLING,
            BackupState.RESUMING,
            BackupState.DOWNLOADING,
            BackupState.ENABLED -> true
            BackupState.WAITING_FOR_SYNC,
            BackupState.DISABLING -> false
        }
}
