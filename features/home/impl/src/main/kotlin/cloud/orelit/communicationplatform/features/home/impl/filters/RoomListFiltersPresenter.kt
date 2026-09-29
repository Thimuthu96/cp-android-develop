/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cloud.orelit.communicationplatform.features.home.impl.filters.selection.FilterSelectionStrategy
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.toImmutableList

@Inject
class RoomListFiltersPresenter(
    private val filterSelectionStrategy: FilterSelectionStrategy,
) : Presenter<RoomListFiltersState> {
    @Composable
    override fun present(): RoomListFiltersState {
        fun handleEvent(event: RoomListFiltersEvent) {
            when (event) {
                RoomListFiltersEvent.ClearSelectedFilters -> {
                    filterSelectionStrategy.clear()
                }
                is RoomListFiltersEvent.ToggleFilter -> {
                    filterSelectionStrategy.toggle(event.filter)
                }
            }
        }

        val filters by filterSelectionStrategy.filterSelectionStates.collectAsState()
        return RoomListFiltersState(
            filterSelectionStates = filters.toImmutableList(),
            eventSink = ::handleEvent,
        )
    }
}
