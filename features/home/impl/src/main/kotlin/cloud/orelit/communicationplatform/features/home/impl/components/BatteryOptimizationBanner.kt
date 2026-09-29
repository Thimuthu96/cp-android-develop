/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
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
import cloud.orelit.communicationplatform.libraries.push.api.battery.BatteryOptimizationEvent
import cloud.orelit.communicationplatform.libraries.push.api.battery.BatteryOptimizationState
import cloud.orelit.communicationplatform.libraries.push.api.battery.aBatteryOptimizationState

@Composable
internal fun BatteryOptimizationBanner(
    state: BatteryOptimizationState,
    modifier: Modifier = Modifier,
) {
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        title = stringResource(R.string.banner_battery_optimization_title_android),
        description = stringResource(R.string.banner_battery_optimization_content_android),
        type = AnnouncementType.Actionable(
            actionText = stringResource(R.string.banner_battery_optimization_submit_android),
            onActionClick = { state.eventSink(BatteryOptimizationEvent.RequestDisableOptimizations) },
            onDismissClick = { state.eventSink(BatteryOptimizationEvent.Dismiss) },
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun BatteryOptimizationBannerPreview() = ElementPreview {
    BatteryOptimizationBanner(
        state = aBatteryOptimizationState(),
    )
}
