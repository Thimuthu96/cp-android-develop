/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.voicemessages.composer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
internal fun VoiceMessageSendingFailedDialog(
    onDismiss: () -> Unit,
) {
    ErrorDialog(
        title = stringResource(CommonStrings.common_error),
        content = stringResource(CommonStrings.error_failed_uploading_voice_message),
        onSubmit = onDismiss,
        submitText = stringResource(CommonStrings.action_ok),
    )
}
