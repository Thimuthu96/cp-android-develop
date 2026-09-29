/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.fullscreenintent.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import cloud.orelit.communicationplatform.libraries.fullscreenintent.impl.FullScreenIntentPermissionsPresenter

@ContributesTo(AppScope::class)
@BindingContainer
interface FullScreenIntentBindingContainer {
    @Binds
    fun bindFullScreenIntentPermissionsPresenter(presenter: FullScreenIntentPermissionsPresenter): Presenter<FullScreenIntentPermissionsState>
}
