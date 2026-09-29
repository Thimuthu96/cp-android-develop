/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.joinroom.impl

import cloud.orelit.communicationplatform.features.joinroom.impl.di.CancelKnockRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeCancelKnockRoom(
    var lambda: (RoomId) -> Result<Unit> = { Result.success(Unit) }
) : CancelKnockRoom {
    override suspend fun invoke(roomId: RoomId) = simulateLongTask {
        lambda(roomId)
    }
}
