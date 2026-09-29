/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.logout.api.LogoutUseCase
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClientProvider
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultLogoutUseCase(
    private val sessionStore: SessionStore,
    private val matrixClientProvider: MatrixClientProvider,
) : LogoutUseCase {
    override suspend fun logoutAll(ignoreSdkError: Boolean) {
        sessionStore.getAllSessions()
            .map { sessionData ->
                SessionId(sessionData.userId)
            }
            .forEach { sessionId ->
                Timber.d("Logging out sessionId: $sessionId")
                matrixClientProvider.getOrRestore(sessionId).fold(
                    onSuccess = { client ->
                        client.logout(userInitiated = true, ignoreSdkError = ignoreSdkError)
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to get or restore MatrixClient for sessionId: $sessionId")
                    }
                )
            }
    }
}
