/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.test

import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.push.api.GetCurrentPushProvider

class FakeGetCurrentPushProvider(
    private val currentPushProvider: String?
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(sessionId: SessionId): String? = currentPushProvider
}
