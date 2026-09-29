/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Inject
class AccountSelectPresenter(
    private val sessionStore: SessionStore,
) : Presenter<AccountSelectState> {
    @Composable
    override fun present(): AccountSelectState {
        val accounts by produceState<ImmutableList<MatrixUser>>(persistentListOf()) {
            // Do not use sessionStore.sessionsFlow() to not make it change when an account is selected.
            value = sessionStore.getAllSessions()
                .map {
                    MatrixUser(
                        userId = UserId(it.userId),
                        displayName = it.userDisplayName,
                        avatarUrl = it.userAvatarUrl,
                    )
                }
                .toImmutableList()
        }

        return AccountSelectState(
            accounts = accounts,
        )
    }
}
