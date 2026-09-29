/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.enterprise.impl

import androidx.compose.ui.graphics.Color
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.features.enterprise.api.BugReportUrl
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.libraries.matrix.api.ClientUrlContentFetcher
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class DefaultEnterpriseService : EnterpriseService {
    override suspend fun isEnterpriseUser(sessionId: SessionId) = false
    override suspend fun tweakMasUrl(url: String, urlContentFetcher: ClientUrlContentFetcher) = url

    // OREL customization: hard-pin the client to the OREL homeserver.
    // A non-empty allow-list makes this the default (and only) account provider and
    // hides the homeserver-selection UI. Auth (account.cp.orel.cloud) and RTC
    // (mrtc.cp.orel.cloud) are discovered from this homeserver's .well-known/OIDC.
    override fun homeserverAllowList(): List<String> = ALLOWED_HOMESERVERS

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean {
        val normalized = homeserverUrl.normalizeHomeserverUrl()
        return ALLOWED_HOMESERVERS.any { it.normalizeHomeserverUrl() == normalized }
    }
    override suspend fun isElementProEnforced(serverName: String): Boolean = false

    override suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?) = Unit

    override fun brandColorsFlow(sessionId: SessionId?): Flow<Color?> {
        return flowOf(null)
    }

    override fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark> {
        return flowOf(SemanticColorsLightDark.default)
    }

    override fun firebasePushGateway(): String? = null
    override fun unifiedPushDefaultPushGateway(): String? = null

    override fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl> {
        return flowOf(BugReportUrl.UseDefault)
    }

    override fun getNoisyNotificationChannelId(sessionId: SessionId): String? = null

    companion object {
        // OREL Matrix homeserver. To allow additional/any homeservers, add more entries
        // or use EnterpriseService.ANY_ACCOUNT_PROVIDER ("*").
        private val ALLOWED_HOMESERVERS = listOf("https://matrix.cp.orel.cloud")
    }
}

/**
 * Normalize a homeserver URL for comparison: lowercase, ensure an https scheme, and drop any
 * trailing slash so "matrix.cp.orel.cloud", "https://matrix.cp.orel.cloud" and
 * "https://matrix.cp.orel.cloud/" all match.
 */
private fun String.normalizeHomeserverUrl(): String {
    val withScheme = if (startsWith("http://") || startsWith("https://")) this else "https://$this"
    return withScheme.trimEnd('/').lowercase()
}
