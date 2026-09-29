/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.analytics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.analytics.api.preferences.AnalyticsPreferencesView
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferencePage
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun AnalyticsSettingsView(
    state: AnalyticsSettingsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_analytics)
    ) {
        AnalyticsPreferencesView(
            state = state.analyticsPreferencesState,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AnalyticsSettingsViewPreview(@PreviewParameter(AnalyticsSettingsStatePreviewParam::class) state: AnalyticsSettingsState) = ElementPreview {
    AnalyticsSettingsView(
        state = state,
        onBackClick = {},
    )
}
