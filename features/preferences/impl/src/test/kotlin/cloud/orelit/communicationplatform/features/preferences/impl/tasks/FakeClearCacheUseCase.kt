/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.tasks

import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeClearCacheUseCase : ClearCacheUseCase {
    var executeHasBeenCalled = false
        private set

    override suspend fun invoke() = simulateLongTask {
        executeHasBeenCalled = true
    }
}
