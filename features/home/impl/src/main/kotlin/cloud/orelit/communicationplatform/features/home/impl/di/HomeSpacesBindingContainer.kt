/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.di

import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesPresenter
import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(SessionScope::class)
interface HomeSpacesBindingContainer {
    @Binds
    fun bindHomeSpacesPresenter(presenter: HomeSpacesPresenter): Presenter<HomeSpacesState>
}
