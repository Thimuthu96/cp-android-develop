/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.impl

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfilePresenterFactory
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileState
import cloud.orelit.communicationplatform.features.userprofile.impl.root.UserProfilePresenter
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

@ContributesBinding(SessionScope::class)
class DefaultUserProfilePresenterFactory(
    private val factory: UserProfilePresenter.Factory,
) : UserProfilePresenterFactory {
    override fun create(userId: UserId): Presenter<UserProfileState> = factory.create(userId)
}
