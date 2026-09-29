/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.auth

import cloud.orelit.communicationplatform.libraries.matrix.api.auth.OAuthPrompt
import org.matrix.rustcomponents.sdk.OAuthPrompt as RustOAuthPrompt

internal fun OAuthPrompt.toRustPrompt(): RustOAuthPrompt {
    return when (this) {
        OAuthPrompt.Login -> RustOAuthPrompt.Unknown("consent")
        OAuthPrompt.Create -> RustOAuthPrompt.Create
        is OAuthPrompt.Unknown -> RustOAuthPrompt.Unknown(value)
    }
}
