/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.enter

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import cloud.orelit.communicationplatform.features.securebackup.impl.setup.views.RecoveryKeyViewState
import cloud.orelit.communicationplatform.features.securebackup.impl.setup.views.aFormattedRecoveryKey
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction

open class SecureBackupEnterRecoveryKeyStatePreviewParam : PreviewParameterProvider<SecureBackupEnterRecoveryKeyState> {
    override val values: Sequence<SecureBackupEnterRecoveryKeyState>
        get() = sequenceOf(
            aSecureBackupEnterRecoveryKeyState(recoveryKey = ""),
            aSecureBackupEnterRecoveryKeyState(),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Loading),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Failure(Exception("A Failure"))),
            aSecureBackupEnterRecoveryKeyState(displayTextFieldContents = false),
        )
}

fun aSecureBackupEnterRecoveryKeyState(
    recoveryKey: String = aFormattedRecoveryKey(),
    isSubmitEnabled: Boolean = recoveryKey.isNotEmpty(),
    displayTextFieldContents: Boolean = true,
    submitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (SecureBackupEnterRecoveryKeyEvent) -> Unit = {},
) = SecureBackupEnterRecoveryKeyState(
    recoveryKeyViewState = RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
        formattedRecoveryKey = recoveryKey,
        displayTextFieldContents = displayTextFieldContents,
        inProgress = submitAction.isLoading(),
    ),
    isSubmitEnabled = isSubmitEnabled,
    submitAction = submitAction,
    eventSink = eventSink,
)
