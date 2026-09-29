/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.error

import androidx.annotation.StringRes
import cloud.orelit.communicationplatform.features.login.impl.R
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.AuthErrorCode
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.AuthenticationException
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.errorCode
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@StringRes
fun loginError(
    throwable: Throwable
): Int {
    val authException = throwable as? AuthenticationException ?: return CommonStrings.error_unknown
    return when (authException.errorCode) {
        AuthErrorCode.FORBIDDEN -> R.string.screen_login_error_invalid_credentials
        AuthErrorCode.USER_DEACTIVATED -> R.string.screen_login_error_deactivated_account
        AuthErrorCode.UNKNOWN -> CommonStrings.error_unknown
    }
}
