/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roommembermoderation.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationState
import cloud.orelit.communicationplatform.features.roommembermoderation.impl.RoomMemberModerationPresenter
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@BindingContainer
interface RoomMemberModerationBindingContainer {
    @Binds
    fun bindRoomMemberModerationPresenter(presenter: RoomMemberModerationPresenter): Presenter<RoomMemberModerationState>
}
