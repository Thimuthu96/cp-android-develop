/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.features.logout.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun LogoutConfirmationDialog(
    onSubmitClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(id = CommonStrings.action_signout),
        content = stringResource(id = R.string.screen_signout_confirmation_dialog_content),
        submitText = stringResource(id = CommonStrings.action_signout),
        onSubmitClick = onSubmitClick,
        onDismiss = onDismiss,
    )
}
