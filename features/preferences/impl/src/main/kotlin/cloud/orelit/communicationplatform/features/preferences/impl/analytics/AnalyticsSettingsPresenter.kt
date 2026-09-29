/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.analytics

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.analytics.api.preferences.AnalyticsPreferencesState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter

@Inject
class AnalyticsSettingsPresenter(
    private val analyticsPreferencesPresenter: Presenter<AnalyticsPreferencesState>,
) : Presenter<AnalyticsSettingsState> {
    @Composable
    override fun present(): AnalyticsSettingsState {
        val analyticsPreferencesState = analyticsPreferencesPresenter.present()

        return AnalyticsSettingsState(
            analyticsPreferencesState = analyticsPreferencesState,
        )
    }
}
