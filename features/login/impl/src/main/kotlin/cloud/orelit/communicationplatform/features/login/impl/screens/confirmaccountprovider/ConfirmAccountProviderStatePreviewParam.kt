/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.confirmaccountprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.login.impl.changeserver.ChangeServerState
import cloud.orelit.communicationplatform.features.login.impl.changeserver.aChangeServerState
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.features.login.impl.login.aLoginModeState
import cloud.orelit.communicationplatform.features.login.impl.screens.createaccount.AccountCreationNotSupported
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData

open class ConfirmAccountProviderStatePreviewParam : PreviewParameterProvider<ConfirmAccountProviderState> {
    override val values: Sequence<ConfirmAccountProviderState>
        get() = sequenceOf(
            aConfirmAccountProviderState(),
            aConfirmAccountProviderState(
                accountProviderInput = "element",
                accountProviderSuggestion = "element.io",
            ),
            aConfirmAccountProviderState(
                isAccountCreation = true,
            ),
            aConfirmAccountProviderState(
                isAccountCreation = true,
                loginModeState = aLoginModeState(loginMode = AsyncData.Failure(AccountCreationNotSupported())),
            ),
        )
}

private fun aConfirmAccountProviderState(
    accountProviderInput: String = "matrix.org",
    accountProviderSuggestion: String? = null,
    isAccountCreation: Boolean = false,
    loginModeState: LoginModeState = aLoginModeState(),
    changeServerState: ChangeServerState = aChangeServerState(),
    eventSink: (ConfirmAccountProviderEvent) -> Unit = {},
) = ConfirmAccountProviderState(
    accountProviderInput = accountProviderInput,
    accountProviderSuggestion = accountProviderSuggestion,
    isAccountCreation = isAccountCreation,
    loginModeState = loginModeState,
    changeServerState = changeServerState,
    eventSink = eventSink
)
