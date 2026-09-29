/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight

@PreviewsDayNight
@Composable
internal fun SecureBackupSetupViewChangePreview(
    @PreviewParameter(SecureBackupSetupStatePreviewParam::class) state: SecureBackupSetupState
) = ElementPreview {
    SecureBackupSetupView(
        state = state.copy(
            isChangeRecoveryKeyUserStory = true,
            recoveryKeyViewState = state.recoveryKeyViewState.copy(recoveryKeyUserStory = RecoveryKeyUserStory.Change),
        ),
        onSuccess = {},
        onBackClick = {},
    )
}
