/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.usersearch.impl

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixSearchUserResults
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class MatrixUserListDataSourceTest {
    @Test
    fun `search - returns users on success`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenSearchUsersResult(
            searchTerm = "test",
            result = Result.success(
                MatrixSearchUserResults(
                    results = persistentListOf(
                        aMatrixUserProfile(),
                        aMatrixUserProfile(userId = A_USER_ID_2)
                    ),
                    limited = false
                )
            )
        )
        val dataSource = MatrixUserListDataSource(matrixClient)

        val results = dataSource.search("test", 2)
        assertThat(results).containsExactly(
            aMatrixUserProfile(),
            aMatrixUserProfile(userId = A_USER_ID_2)
        )
    }

    @Test
    fun `search - returns empty list on error`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenSearchUsersResult(
            searchTerm = "test",
            result = Result.failure(RuntimeException("Ruhroh"))
        )
        val dataSource = MatrixUserListDataSource(matrixClient)

        val results = dataSource.search("test", 2)
        assertThat(results).isEmpty()
    }

    @Test
    fun `get profile - returns user on success`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenGetProfileResult(
            userId = A_USER_ID,
            result = Result.success(aMatrixUserProfile())
        )
        val dataSource = MatrixUserListDataSource(matrixClient)

        val result = dataSource.getProfile(A_USER_ID)
        assertThat(result).isEqualTo(aMatrixUserProfile())
    }

    @Test
    fun `get profile - returns null on error`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenGetProfileResult(
            userId = A_USER_ID,
            result = Result.failure(RuntimeException("Ruhroh"))
        )
        val dataSource = MatrixUserListDataSource(matrixClient)

        val result = dataSource.getProfile(A_USER_ID)
        assertThat(result).isNull()
    }

    private fun aMatrixUserProfile(
        userId: UserId = A_USER_ID,
        displayName: String = A_USER_NAME,
        avatarUrl: String = AN_AVATAR_URL
    ) = MatrixUser(userId, displayName, avatarUrl)
}
