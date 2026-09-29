/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.pushproviders.api.Config
import cloud.orelit.communicationplatform.libraries.pushstore.api.clientsecret.PushClientSecret

interface UnifiedPushSessionPushConfigProvider {
    suspend fun provide(sessionId: SessionId): Config?
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushPushConfigProvider(
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
) : UnifiedPushSessionPushConfigProvider {
    override suspend fun provide(sessionId: SessionId): Config? {
        val clientSecret = pushClientSecret.getSecretForUser(sessionId)
        val url = unifiedPushStore.getPushGateway(clientSecret) ?: return null
        val pushKey = unifiedPushStore.getEndpoint(clientSecret) ?: return null
        return Config(
            url = url,
            pushKey = pushKey,
        )
    }
}
