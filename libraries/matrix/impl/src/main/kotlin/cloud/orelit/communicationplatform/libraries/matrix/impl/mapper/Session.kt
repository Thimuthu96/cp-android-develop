/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.mapper

import cloud.orelit.communicationplatform.libraries.matrix.api.paths.SessionPaths
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.LoginType
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionData
import org.matrix.rustcomponents.sdk.Session
import java.util.Date

internal fun Session.toSessionData(
    isTokenValid: Boolean,
    loginType: LoginType,
    passphrase: String?,
    sessionPaths: SessionPaths,
    homeserverUrl: String? = null,
) = SessionData(
    userId = userId,
    deviceId = deviceId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    homeserverUrl = homeserverUrl ?: this.homeserverUrl,
    oAuthData = oauthData,
    loginTimestamp = Date(),
    isTokenValid = isTokenValid,
    loginType = loginType,
    passphrase = passphrase,
    sessionPath = sessionPaths.fileDirectory.absolutePath,
    cachePath = sessionPaths.cacheDirectory.absolutePath,
    // Note: position and lastUsageIndex will be set by the SessionStore when adding the session
    position = 0,
    lastUsageIndex = 0,
    userDisplayName = null,
    userAvatarUrl = null,
)
