/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.analytics.impl.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.appconfig.AnalyticsConfig
import cloud.orelit.communicationplatform.features.analytics.api.AnalyticsOptInEvent
import cloud.orelit.communicationplatform.features.analytics.api.preferences.AnalyticsPreferencesState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class AnalyticsPreferencesPresenter(
    private val analyticsService: AnalyticsService,
    private val buildMeta: BuildMeta,
) : Presenter<AnalyticsPreferencesState> {
    @Composable
    override fun present(): AnalyticsPreferencesState {
        val localCoroutineScope = rememberCoroutineScope()
        val isEnabled = analyticsService.userConsentFlow.collectAsState(initial = false)

        fun handleEvent(event: AnalyticsOptInEvent) {
            when (event) {
                is AnalyticsOptInEvent.EnableAnalytics -> localCoroutineScope.setIsEnabled(event.isEnabled)
            }
        }

        return AnalyticsPreferencesState(
            applicationName = buildMeta.applicationName,
            isEnabled = isEnabled.value,
            policyUrl = AnalyticsConfig.POLICY_LINK,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.setIsEnabled(enabled: Boolean) = launch {
        analyticsService.setUserConsent(enabled)
    }
}
