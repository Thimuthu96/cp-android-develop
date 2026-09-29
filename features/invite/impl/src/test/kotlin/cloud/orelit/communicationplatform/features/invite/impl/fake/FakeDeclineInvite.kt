/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invite.impl.fake

import cloud.orelit.communicationplatform.features.invite.impl.DeclineInvite
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeDeclineInvite(
    private val lambda: (RoomId, Boolean, Boolean, String?) -> Result<RoomId> = { _, _, _, _ -> lambdaError() },
) : DeclineInvite {
    override suspend fun invoke(roomId: RoomId, blockUser: Boolean, reportRoom: Boolean, reportReason: String?): Result<RoomId> = simulateLongTask {
        lambda(roomId, blockUser, reportRoom, reportReason)
    }
}
