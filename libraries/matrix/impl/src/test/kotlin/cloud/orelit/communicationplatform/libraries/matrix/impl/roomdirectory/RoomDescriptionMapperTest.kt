/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.roomdirectory

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.roomdirectory.RoomDescription
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.factories.aRustRoomDescription
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ALIAS
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.roomdirectory.aRoomDescription
import org.junit.Test
import org.matrix.rustcomponents.sdk.PublicRoomJoinRule

class RoomDescriptionMapperTest {
    @Test
    fun map() {
        assertThat(RoomDescriptionMapper().map(aRustRoomDescription())).isEqualTo(
            aRoomDescription(
                roomId = A_ROOM_ID,
                name = "name",
                topic = "topic",
                alias = A_ROOM_ALIAS,
                avatarUrl = "avatarUrl",
                joinRule = RoomDescription.JoinRule.PUBLIC,
                isWorldReadable = true,
                joinedMembers = 2L
            )
        )
    }

    @Test
    fun mapWithNullAlias() {
        assertThat(RoomDescriptionMapper().map(aRustRoomDescription().copy(alias = null)).alias).isNull()
    }

    @Test
    fun `map join rule`() {
        assertThat(PublicRoomJoinRule.PUBLIC.map()).isEqualTo(RoomDescription.JoinRule.PUBLIC)
        assertThat(PublicRoomJoinRule.KNOCK.map()).isEqualTo(RoomDescription.JoinRule.KNOCK)
        assertThat(null.map()).isEqualTo(RoomDescription.JoinRule.UNKNOWN)
    }
}
