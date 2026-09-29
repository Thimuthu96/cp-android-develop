/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.di

import cloud.orelit.communicationplatform.features.home.impl.filters.RoomListFiltersPresenter
import cloud.orelit.communicationplatform.features.home.impl.filters.RoomListFiltersState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListPresenter
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListState
import cloud.orelit.communicationplatform.features.home.impl.search.GlobalSearchPresenter
import cloud.orelit.communicationplatform.features.home.impl.search.GlobalSearchState
import cloud.orelit.communicationplatform.features.home.impl.search.RoomListSearchPresenter
import cloud.orelit.communicationplatform.features.home.impl.search.RoomListSearchState
import cloud.orelit.communicationplatform.features.home.impl.spacefilters.SpaceFiltersPresenter
import cloud.orelit.communicationplatform.features.home.impl.spacefilters.SpaceFiltersState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(SessionScope::class)
@BindingContainer
interface RoomListBindingContainer {
    @Binds
    fun bindRoomListPresenter(presenter: RoomListPresenter): Presenter<RoomListState>

    @Binds
    fun bindRoomListSearchPresenter(presenter: RoomListSearchPresenter): Presenter<RoomListSearchState>

    @Binds
    fun bindRoomListFiltersPresenter(presenter: RoomListFiltersPresenter): Presenter<RoomListFiltersState>

    @Binds
    fun bindSpaceFiltersPresenter(presenter: SpaceFiltersPresenter): Presenter<SpaceFiltersState>

    @Binds
    fun bindGlobalSearchPresenter(presenter: GlobalSearchPresenter): Presenter<GlobalSearchState>
}
