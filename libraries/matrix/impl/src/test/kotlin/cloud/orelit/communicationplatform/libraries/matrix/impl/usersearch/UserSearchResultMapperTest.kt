/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.usersearch

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixSearchUserResults
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.factories.aRustSearchUsersResults
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.factories.aRustUserProfile
import cloud.orelit.communicationplatform.libraries.matrix.impl.user.UserSearchResultMapper
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class UserSearchResultMapperTest {
    @Test
    fun `map limited list`() {
        assertThat(
            UserSearchResultMapper.map(
                aRustSearchUsersResults(
                    results = listOf(aRustUserProfile(A_USER_ID.value, "displayName", "avatarUrl")),
                    limited = true,
                )
            )
        )
            .isEqualTo(
                MatrixSearchUserResults(
                    results = listOf(MatrixUser(A_USER_ID, "displayName", "avatarUrl")).toImmutableList(),
                    limited = true,
                )
            )
    }

    @Test
    fun `map not limited list`() {
        assertThat(
            UserSearchResultMapper.map(
                aRustSearchUsersResults(
                    results = listOf(aRustUserProfile(A_USER_ID.value, "displayName", "avatarUrl")),
                    limited = false,
                )
            )
        )
            .isEqualTo(
                MatrixSearchUserResults(
                    results = listOf(MatrixUser(A_USER_ID, "displayName", "avatarUrl")).toImmutableList(),
                    limited = false,
                )
            )
    }
}
