/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.annotations.ApplicationContext
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.pushproviders.api.Distributor
import cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.unifiedpush.android.connector.UnifiedPush
import kotlin.time.Duration.Companion.seconds

interface RegisterUnifiedPushUseCase {
    suspend fun execute(distributor: Distributor, clientSecret: String, sessionId: SessionId): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultRegisterUnifiedPushUseCase(
    @ApplicationContext private val context: Context,
    private val endpointRegistrationHandler: EndpointRegistrationHandler,
) : RegisterUnifiedPushUseCase {
    override suspend fun execute(distributor: Distributor, clientSecret: String, sessionId: SessionId): Result<Unit> {
        UnifiedPush.saveDistributor(context, distributor.value)
        // This will trigger the callback
        // VectorUnifiedPushMessagingReceiver.onNewEndpoint
        UnifiedPush.register(context = context, instance = clientSecret, messageForDistributor = sessionId.value)
        // Wait for VectorUnifiedPushMessagingReceiver.onNewEndpoint to proceed
        @Suppress("RunCatchingNotAllowed")
        return runCatching {
            withTimeout(30.seconds) {
                val result = endpointRegistrationHandler.state
                    .filter { it.clientSecret == clientSecret }
                    .first()
                    .result
                result.getOrThrow()
            }
        }
    }
}
