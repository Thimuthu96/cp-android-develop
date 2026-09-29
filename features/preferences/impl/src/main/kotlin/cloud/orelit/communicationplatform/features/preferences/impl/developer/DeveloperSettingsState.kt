/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.developer

import cloud.orelit.communicationplatform.features.preferences.impl.developer.appsettings.AppDeveloperSettingsState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.DeviceId
import kotlinx.collections.immutable.ImmutableMap

data class DeveloperSettingsState(
    val appDeveloperSettingsState: AppDeveloperSettingsState,
    val cacheSize: AsyncData<String>,
    val databaseSizes: AsyncData<ImmutableMap<String, String>>,
    val clearCacheAction: AsyncAction<Unit>,
    val markAllRoomsAsReadAction: AsyncAction<Unit>,
    val pushRulesAction: AsyncAction<Unit>,
    val isEnterpriseBuild: Boolean,
    val showColorPicker: Boolean,
    val deviceId: DeviceId,
    val eventSink: (DeveloperSettingsEvent) -> Unit
) {
    val showLoader = clearCacheAction is AsyncAction.Loading ||
        markAllRoomsAsReadAction is AsyncAction.Loading ||
        pushRulesAction is AsyncAction.Loading
}
