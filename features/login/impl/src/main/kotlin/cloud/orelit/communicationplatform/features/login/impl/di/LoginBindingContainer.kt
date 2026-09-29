/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.login.impl.changeserver.ChangeServerPresenter
import cloud.orelit.communicationplatform.features.login.impl.changeserver.ChangeServerState
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModePresenter
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter

@ContributesTo(AppScope::class)
@BindingContainer
interface LoginBindingContainer {
    @Binds
    fun bindChangeServerPresenter(presenter: ChangeServerPresenter): Presenter<ChangeServerState>

    @Binds
    fun bindLoginModePresenter(presenter: LoginModePresenter): Presenter<LoginModeState>
}
