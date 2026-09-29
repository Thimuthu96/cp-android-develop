/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.test

import cloud.orelit.communicationplatform.features.location.api.live.ActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

class FakeActiveLiveLocationShareManager(
    val setupLambda: () -> Unit = { lambdaError() },
    val startShareLambda: (roomId: RoomId, duration: Duration) -> Result<Unit> = { _, _ -> lambdaError() },
    val stopShareLambda: (roomId: RoomId) -> Result<Unit> = { _ -> lambdaError() },
) : ActiveLiveLocationShareManager {
    private val _sharingRoomIds = MutableStateFlow(emptySet<RoomId>())
    override val sharingRoomIds: StateFlow<Set<RoomId>> = _sharingRoomIds

    override suspend fun setup() {
        setupLambda()
    }

    override suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit> = simulateLongTask {
        startShareLambda(roomId, duration).onSuccess {
            _sharingRoomIds.update {
                it + roomId
            }
        }
    }

    override suspend fun stopShare(roomId: RoomId): Result<Unit> = simulateLongTask {
        stopShareLambda(roomId).onSuccess {
            _sharingRoomIds.update {
                it - roomId
            }
        }
    }
}
