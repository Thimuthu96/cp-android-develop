/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x.oidc

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.OAuthRedirectUrlProvider
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import cloud.orelit.communicationplatform.x.R

@ContributesBinding(AppScope::class)
class DefaultOAuthRedirectUrlProvider(
    private val stringProvider: StringProvider,
) : OAuthRedirectUrlProvider {
    override fun provide() = buildString {
        append(stringProvider.getString(R.string.login_redirect_scheme))
        append(":/")
    }
}
