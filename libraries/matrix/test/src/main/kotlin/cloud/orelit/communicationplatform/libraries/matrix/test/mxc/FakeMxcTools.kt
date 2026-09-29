/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.mxc

import cloud.orelit.communicationplatform.libraries.matrix.api.mxc.MxcTools
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeMxcTools(
    private val mxcUri2FilePathResult: (String) -> String? = { lambdaError() }
) : MxcTools {
    override fun mxcUri2FilePath(mxcUri: String): String? {
        return mxcUri2FilePathResult(mxcUri)
    }
}
