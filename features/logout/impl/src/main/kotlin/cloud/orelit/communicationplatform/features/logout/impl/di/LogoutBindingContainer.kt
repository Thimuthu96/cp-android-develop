/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutState
import cloud.orelit.communicationplatform.features.logout.impl.direct.DirectLogoutPresenter
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface LogoutBindingContainer {
    @Binds
    fun bindDirectLogoutPresenter(presenter: DirectLogoutPresenter): Presenter<DirectLogoutState>
}
