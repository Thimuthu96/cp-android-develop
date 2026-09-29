/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.classic

import android.content.Intent
import android.content.ServiceConnection
import cloud.orelit.communicationplatform.libraries.androidutils.service.ServiceBinder
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeServiceBinder(
    private val bindServiceResult: () -> Boolean = { lambdaError() },
    private val unbindServiceResult: () -> Unit = { lambdaError() },
) : ServiceBinder {
    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        return bindServiceResult()
    }

    override fun unbindService(conn: ServiceConnection) {
        unbindServiceResult()
    }
}
