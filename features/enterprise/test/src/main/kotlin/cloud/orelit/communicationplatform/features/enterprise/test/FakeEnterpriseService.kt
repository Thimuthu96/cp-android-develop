/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.enterprise.test

import androidx.compose.ui.graphics.Color
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.features.enterprise.api.BugReportUrl
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.libraries.matrix.api.ClientUrlContentFetcher
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEnterpriseService(
    private val isEnterpriseUserResult: (SessionId) -> Boolean = { lambdaError() },
    private val defaultHomeserverListResult: () -> List<String> = { emptyList() },
    private val isAllowedToConnectToHomeserverResult: (String) -> Boolean = { lambdaError() },
    initialSemanticColors: SemanticColorsLightDark = SemanticColorsLightDark.default,
    initialBrandColor: Color? = null,
    private val overrideBrandColorResult: (SessionId?, String?) -> Unit = { _, _ -> lambdaError() },
    private val firebasePushGatewayResult: () -> String? = { lambdaError() },
    private val unifiedPushDefaultPushGatewayResult: () -> String? = { lambdaError() },
    private val getNoisyNotificationChannelIdResult: (SessionId?) -> String? = { lambdaError() },
    private val tweakMasUrlResult: (String, ClientUrlContentFetcher) -> String = { _, _ -> lambdaError() },
    private val isElementProEnforcedResult: (String) -> Boolean = { lambdaError() },
) : EnterpriseService {
    private val brandColorState = MutableStateFlow(initialBrandColor)
    private val semanticColorsState = MutableStateFlow(initialSemanticColors)

    override suspend fun isEnterpriseUser(sessionId: SessionId): Boolean = simulateLongTask {
        isEnterpriseUserResult(sessionId)
    }

    override suspend fun tweakMasUrl(url: String, urlContentFetcher: ClientUrlContentFetcher): String = simulateLongTask {
        tweakMasUrlResult(url, urlContentFetcher)
    }

    override fun homeserverAllowList(): List<String> {
        return defaultHomeserverListResult()
    }

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean = simulateLongTask {
        isAllowedToConnectToHomeserverResult(homeserverUrl)
    }

    override suspend fun isElementProEnforced(serverName: String): Boolean = simulateLongTask {
        isElementProEnforcedResult(serverName)
    }

    override suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?) = simulateLongTask {
        overrideBrandColorResult(sessionId, brandColor)
    }

    override fun brandColorsFlow(sessionId: SessionId?): Flow<Color?> {
        return brandColorState.asStateFlow()
    }

    override fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark> {
        return semanticColorsState.asStateFlow()
    }

    override fun firebasePushGateway(): String? {
        return firebasePushGatewayResult()
    }

    override fun unifiedPushDefaultPushGateway(): String? {
        return unifiedPushDefaultPushGatewayResult()
    }

    val bugReportUrlMutableFlow = MutableStateFlow<BugReportUrl>(BugReportUrl.UseDefault)
    override fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl> {
        return bugReportUrlMutableFlow.asStateFlow()
    }

    override fun getNoisyNotificationChannelId(sessionId: SessionId): String? {
        return getNoisyNotificationChannelIdResult(sessionId)
    }
}
