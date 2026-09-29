/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.signedout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.LoginType
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionData

open class SignedOutStatePreviewParam : PreviewParameterProvider<SignedOutState> {
    override val values: Sequence<SignedOutState>
        get() = sequenceOf(
            aSignedOutState(),
            // Add other states here
        )
}

private fun aSignedOutState() = SignedOutState(
    appName = "AppName",
    signedOutSession = aSessionData(),
    eventSink = {},
)

private fun aSessionData(
    sessionId: String = "@alice:server.org",
    isTokenValid: Boolean = false,
): SessionData {
    return SessionData(
        userId = sessionId,
        deviceId = "aDeviceId",
        accessToken = "anAccessToken",
        refreshToken = "aRefreshToken",
        homeserverUrl = "aHomeserverUrl",
        oAuthData = null,
        loginTimestamp = null,
        isTokenValid = isTokenValid,
        loginType = LoginType.UNKNOWN,
        passphrase = null,
        sessionPath = "/a/path/to/a/session",
        cachePath = "/a/path/to/a/cache",
        position = 0,
        lastUsageIndex = 0,
        userDisplayName = null,
        userAvatarUrl = null,
    )
}
