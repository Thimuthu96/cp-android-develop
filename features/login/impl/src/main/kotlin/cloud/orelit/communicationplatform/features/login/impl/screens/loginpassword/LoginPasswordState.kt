/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.loginpassword

import android.os.Parcelable
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import kotlinx.parcelize.Parcelize

data class LoginPasswordState(
    val accountProvider: AccountProvider,
    val formState: LoginFormState,
    val loginAction: AsyncData<SessionId>,
    val eventSink: (LoginPasswordEvent) -> Unit
) {
    val submitEnabled: Boolean
        get() = loginAction !is AsyncData.Failure &&
            formState.login.isNotEmpty() &&
            formState.password.isNotEmpty()
}

@Parcelize
data class LoginFormState(
    val login: String,
    val password: String,
) : Parcelable {
    companion object {
        val Default = LoginFormState("", "")
    }
}
