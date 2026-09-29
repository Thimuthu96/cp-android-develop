/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.developer.appsettings

import cloud.orelit.communicationplatform.features.preferences.impl.developer.tracing.LogLevelItem
import cloud.orelit.communicationplatform.features.rageshake.api.preferences.RageshakePreferencesState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.featureflag.ui.model.FeatureUiModel
import cloud.orelit.communicationplatform.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.collections.immutable.ImmutableList

data class AppDeveloperSettingsState(
    val features: ImmutableList<FeatureUiModel>,
    val rageshakeState: RageshakePreferencesState,
    val customElementCallBaseUrlState: CustomElementCallBaseUrlState,
    val tracingLogLevel: AsyncData<LogLevelItem>,
    val tracingLogPacks: ImmutableList<TraceLogPack>,
    val gitBranch: String,
    val gitSha: String,
    val eventSink: (AppDeveloperSettingsEvent) -> Unit
)

data class CustomElementCallBaseUrlState(
    val baseUrl: String?,
    val validator: (String?) -> Boolean,
)
