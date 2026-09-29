/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.loginpassword

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProvider
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.anAccountProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId

open class LoginPasswordStatePreviewParam : PreviewParameterProvider<LoginPasswordState> {
    override val values: Sequence<LoginPasswordState>
        get() = sequenceOf(
            aLoginPasswordState(),
            // Loading
            aLoginPasswordState(loginAction = AsyncData.Loading()),
            // Error
            aLoginPasswordState(loginAction = AsyncData.Failure(Exception("An error occurred"))),
        )
}

fun aLoginPasswordState(
    accountProvider: AccountProvider = anAccountProvider(),
    formState: LoginFormState = LoginFormState.Default,
    loginAction: AsyncData<SessionId> = AsyncData.Uninitialized,
    eventSink: (LoginPasswordEvent) -> Unit = {},
) = LoginPasswordState(
    accountProvider = accountProvider,
    formState = formState,
    loginAction = loginAction,
    eventSink = eventSink,
)

fun aLoginFormState(
    login: String = "",
    password: String = "",
) = LoginFormState(
    login = login,
    password = password,
)
