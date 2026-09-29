/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.accountprovider

import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.test.FakeEnterpriseService
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import cloud.orelit.communicationplatform.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun anAccountProviderDataSource(
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) = AccountProviderDataSource(
    enterpriseService = enterpriseService,
    appPreferencesStore = appPreferencesStore,
    coroutineScope = coroutineScope,
)
