/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.userstatus.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.preferences.impl.userstatus.UserStatusPresenter
import cloud.orelit.communicationplatform.features.preferences.impl.userstatus.UserStatusState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesTo(SessionScope::class)
@BindingContainer
interface UserStatusBindingContainer {
    @Binds
    fun bindUserStatusPresenter(presenter: UserStatusPresenter): Presenter<UserStatusState>
}
