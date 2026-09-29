/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.reportroom.impl.fakes

import cloud.orelit.communicationplatform.features.reportroom.impl.ReportRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeReportRoom(
    var lambda: (RoomId, Boolean, String, Boolean) -> Result<Unit> = { _, _, _, _ -> lambdaError() }
) : ReportRoom {
    override suspend fun invoke(
        roomId: RoomId,
        shouldReport: Boolean,
        reason: String,
        shouldLeave: Boolean
    ): Result<Unit> = simulateLongTask {
        lambda(roomId, shouldReport, reason, shouldLeave)
    }
}
