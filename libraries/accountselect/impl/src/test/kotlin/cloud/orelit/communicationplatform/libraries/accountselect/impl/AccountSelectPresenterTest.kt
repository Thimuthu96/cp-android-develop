/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.accountselect.impl

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID_2
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aSessionData
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountSelectPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAccountSelectPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.accounts).isEmpty()
        }
    }

    @Test
    fun `present - multiple accounts case`() = runTest {
        val presenter = createAccountSelectPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(sessionId = A_SESSION_ID.value),
                    aSessionData(
                        sessionId = A_SESSION_ID_2.value,
                        userDisplayName = "Bob",
                        userAvatarUrl = "avatarUrl",
                    ),
                )
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.accounts).hasSize(2)
            val firstAccount = initialState.accounts[0]
            assertThat(firstAccount).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID,
                    displayName = null,
                    avatarUrl = null,
                )
            )
            val secondAccount = initialState.accounts[1]
            assertThat(secondAccount).isEqualTo(
                MatrixUser(
                    userId = A_SESSION_ID_2,
                    displayName = "Bob",
                    avatarUrl = "avatarUrl",
                )
            )
        }
    }
}

internal fun createAccountSelectPresenter(
    sessionStore: SessionStore = InMemorySessionStore(),
) = AccountSelectPresenter(
    sessionStore = sessionStore,
)
