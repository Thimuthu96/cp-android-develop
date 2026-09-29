/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush.troubleshoot

import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.pushproviders.api.Config
import cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush.UnifiedPushSessionPushConfigProvider
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeUnifiedPushSessionPushConfigProvider(
    private val config: (SessionId) -> Config? = { lambdaError() },
) : UnifiedPushSessionPushConfigProvider {
    override suspend fun provide(sessionId: SessionId): Config? {
        return config(sessionId)
    }
}
