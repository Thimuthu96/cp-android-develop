/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.login.impl.error.ChangeServerErrorPreviewParam
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.AuthenticationException

class LoginModeViewErrorPreviewParam : PreviewParameterProvider<Exception> {
    override val values: Sequence<Exception>
        get() = ChangeServerErrorPreviewParam().values +
            AuthenticationException.AccountAlreadyLoggedIn("@alice:matrix.org")
}
