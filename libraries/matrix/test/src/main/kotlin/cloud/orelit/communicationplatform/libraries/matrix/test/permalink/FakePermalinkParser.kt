/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.permalink

import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakePermalinkParser(
    private var result: (String) -> PermalinkData = { lambdaError() }
) : PermalinkParser {
    fun givenResult(result: PermalinkData) {
        this.result = { result }
    }

    override fun parse(uriString: String): PermalinkData {
        return result(uriString)
    }
}
