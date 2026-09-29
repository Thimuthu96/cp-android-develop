/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.chooseaccountprovider

import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProvider

sealed interface ChooseAccountProviderEvent {
    data class SelectAccountProvider(val accountProvider: AccountProvider) : ChooseAccountProviderEvent
    data object Continue : ChooseAccountProviderEvent
    data object ClearError : ChooseAccountProviderEvent
}
