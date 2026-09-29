/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl.filters

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.filters.selection.FilterSelectionState
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.pressTag
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class RoomListFiltersViewTest : RobolectricTest() {
    @Test
    fun `clicking on filters generates expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvent>()
        setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(eventSink = eventsRecorder),
            )
        }
        clickOn(R.string.screen_roomlist_filter_rooms)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms),
            )
        )
    }

    @Test
    fun `clicking on clear filters generates expected Event`() = runAndroidComposeUiTest<ComponentActivity> {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvent>()
        setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(
                    filterSelectionStates = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = true) },
                    eventSink = eventsRecorder
                ),
            )
        }
        pressTag(TestTags.homeScreenClearFilters.value)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvent.ClearSelectedFilters,
            )
        )
    }
}
