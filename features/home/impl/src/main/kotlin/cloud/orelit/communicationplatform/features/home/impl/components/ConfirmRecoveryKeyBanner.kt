/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.Announcement
import cloud.orelit.communicationplatform.libraries.designsystem.components.AnnouncementType
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
internal fun ConfirmRecoveryKeyBanner(
    onContinueClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        title = stringResource(R.string.confirm_recovery_key_banner_title),
        description = stringResource(R.string.confirm_recovery_key_banner_message),
        type = AnnouncementType.Actionable(
            actionText = stringResource(CommonStrings.action_continue),
            onActionClick = onContinueClick,
            onDismissClick = onDismissClick,
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun ConfirmRecoveryKeyBannerPreview() = ElementPreview {
    ConfirmRecoveryKeyBanner(
        onContinueClick = {},
        onDismissClick = {},
    )
}
