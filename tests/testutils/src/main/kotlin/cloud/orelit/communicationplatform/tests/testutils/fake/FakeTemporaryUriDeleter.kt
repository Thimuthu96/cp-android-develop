/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.tests.testutils.fake

import android.net.Uri
import cloud.orelit.communicationplatform.libraries.androidutils.file.TemporaryUriDeleter
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeTemporaryUriDeleter(
    val deleteLambda: (uri: Uri?) -> Unit = { lambdaError() }
) : TemporaryUriDeleter {
    override fun delete(uri: Uri?) {
        deleteLambda(uri)
    }
}
