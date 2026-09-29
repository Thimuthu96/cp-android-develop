/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.preferences.impl.store.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.preferences.api.store.SessionPreferencesStore
import cloud.orelit.communicationplatform.libraries.preferences.impl.store.DefaultSessionPreferencesStoreFactory
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(SessionScope::class)
object SessionPreferencesBindingContainer {
    @Provides
    fun providesSessionPreferencesStore(
        defaultSessionPreferencesStoreFactory: DefaultSessionPreferencesStoreFactory,
        sessionId: SessionId,
        @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    ): SessionPreferencesStore {
        return defaultSessionPreferencesStoreFactory
            .get(sessionId, sessionCoroutineScope)
    }
}
