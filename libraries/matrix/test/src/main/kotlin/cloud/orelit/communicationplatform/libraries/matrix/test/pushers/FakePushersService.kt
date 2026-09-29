/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.pushers

import cloud.orelit.communicationplatform.libraries.matrix.api.pusher.PushersService
import cloud.orelit.communicationplatform.libraries.matrix.api.pusher.SetHttpPusherData
import cloud.orelit.communicationplatform.libraries.matrix.api.pusher.UnsetHttpPusherData
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakePushersService(
    private val setHttpPusherResult: (SetHttpPusherData) -> Result<Unit> = { lambdaError() },
    private val unsetHttpPusherResult: (UnsetHttpPusherData) -> Result<Unit> = { lambdaError() },
) : PushersService {
    override suspend fun setHttpPusher(setHttpPusherData: SetHttpPusherData) = setHttpPusherResult(setHttpPusherData)
    override suspend fun unsetHttpPusher(unsetHttpPusherData: UnsetHttpPusherData): Result<Unit> = unsetHttpPusherResult(unsetHttpPusherData)
}
