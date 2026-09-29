/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeRoomDetailsNavigator(
    private val onDoneResult: () -> Unit = { lambdaError() }
) : RoomDetailsNavigator {
    override fun onDone() = onDoneResult()
}
