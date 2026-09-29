/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.api.room

import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

/**
 * Try to find an existing DM with the given user, or create one if none exists and [createIfDmDoesNotExist] is true.
 */
suspend fun MatrixClient.startDM(
    userId: UserId,
    createIfDmDoesNotExist: Boolean,
    isEncryped: Boolean,
): StartDMResult {
    return findDM(userId)
        .fold(
            onSuccess = { existingDM ->
                if (existingDM != null) {
                    StartDMResult.Success(existingDM, isNew = false)
                } else if (createIfDmDoesNotExist) {
                    createDM(userId, isEncryped).fold(
                        { StartDMResult.Success(it, isNew = true) },
                        { StartDMResult.Failure(it) }
                    )
                } else {
                    StartDMResult.DmDoesNotExist
                }
            },
            onFailure = { error ->
                StartDMResult.Failure(error)
            }
        )
}

sealed interface StartDMResult {
    data class Success(val roomId: RoomId, val isNew: Boolean) : StartDMResult
    data object DmDoesNotExist : StartDMResult
    data class Failure(val throwable: Throwable) : StartDMResult
}
