/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
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
import cloud.orelit.communicationplatform.libraries.fullscreenintent.api.FullScreenIntentPermissionsEvent
import cloud.orelit.communicationplatform.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import cloud.orelit.communicationplatform.libraries.fullscreenintent.api.aFullScreenIntentPermissionsState
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun FullScreenIntentPermissionBanner(
    state: FullScreenIntentPermissionsState,
    modifier: Modifier = Modifier
) {
    Announcement(
        title = stringResource(R.string.full_screen_intent_banner_title),
        description = stringResource(R.string.full_screen_intent_banner_message),
        type = AnnouncementType.Actionable(
            actionText = stringResource(CommonStrings.action_continue),
            onDismissClick = { state.eventSink(FullScreenIntentPermissionsEvent.Dismiss) },
            onActionClick = { state.eventSink(FullScreenIntentPermissionsEvent.OpenSettings) },
        ),
        modifier = modifier.roomListBannerPadding(),
    )
}

@PreviewsDayNight
@Composable
internal fun FullScreenIntentPermissionBannerPreview() {
    ElementPreview {
        FullScreenIntentPermissionBanner(aFullScreenIntentPermissionsState())
    }
}
