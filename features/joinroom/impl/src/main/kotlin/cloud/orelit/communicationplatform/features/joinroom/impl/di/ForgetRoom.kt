/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.joinroom.impl.di

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

interface ForgetRoom {
    suspend operator fun invoke(roomId: RoomId): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultForgetRoom(private val client: MatrixClient) : ForgetRoom {
    override suspend fun invoke(roomId: RoomId): Result<Unit> {
        return client.getRoom(roomId)?.use { it.forget() }
            ?: Result.failure(IllegalStateException("Room not found"))
    }
}
