/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.chooseaccountprovider

import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProvider
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class ChooseAccountProviderState(
    val accountProviders: ImmutableList<AccountProvider>,
    val selectedAccountProvider: AccountProvider?,
    val loginModeState: LoginModeState,
    val eventSink: (ChooseAccountProviderEvent) -> Unit,
) {
    val submitEnabled: Boolean
        get() = selectedAccountProvider != null &&
            (loginModeState.loginMode is AsyncData.Uninitialized || loginModeState.loginMode is AsyncData.Loading)
}
