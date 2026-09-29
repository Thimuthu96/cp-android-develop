/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl.roomlist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.model.aRoomListRoomSummary
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.setSafeContent
import org.junit.Test

class RoomListDeclineInviteMenuTest : RobolectricTest() {
    @Test
    fun `clicking on decline emits the expected Events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val menu = RoomListState.DeclineInviteMenu.Shown(roomSummary = aRoomListRoomSummary())
        setSafeContent {
            RoomListDeclineInviteMenu(
                menu = menu,
                canReportRoom = false,
                onDeclineAndBlockClick = EnsureNeverCalledWithParam(),
                eventSink = eventsRecorder,
            )
        }
        clickOn(CommonStrings.action_decline)
        eventsRecorder.assertList(
            listOf(
                RoomListEvent.HideDeclineInviteMenu,
                RoomListEvent.DeclineInvite(menu.roomSummary, blockUser = false),
            )
        )
    }

    @Test
    fun `clicking on decline and block when canReportRoom=true, it emits the expected Events and callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val menu = RoomListState.DeclineInviteMenu.Shown(roomSummary = aRoomListRoomSummary())
        setSafeContent {
            RoomListDeclineInviteMenu(
                menu = menu,
                canReportRoom = true,
                onDeclineAndBlockClick = EnsureCalledOnceWithParam(menu.roomSummary, Unit),
                eventSink = eventsRecorder,
            )
        }
        clickOn(CommonStrings.action_decline_and_block)
        val expectedEvents = listOf(RoomListEvent.HideDeclineInviteMenu)
        eventsRecorder.assertList(expectedEvents)
    }

    @Test
    fun `clicking on decline and block when canReportRoom=false, it emits the expected Events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val menu = RoomListState.DeclineInviteMenu.Shown(roomSummary = aRoomListRoomSummary())
        setSafeContent {
            RoomListDeclineInviteMenu(
                menu = menu,
                canReportRoom = false,
                onDeclineAndBlockClick = EnsureNeverCalledWithParam(),
                eventSink = eventsRecorder,
            )
        }
        clickOn(CommonStrings.action_decline_and_block)
        val expectedEvents = listOf(
            RoomListEvent.HideDeclineInviteMenu,
            RoomListEvent.DeclineInvite(menu.roomSummary, blockUser = true),
        )
        eventsRecorder.assertList(expectedEvents)
    }

    @Test
    fun `clicking on cancel emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val menu = RoomListState.DeclineInviteMenu.Shown(roomSummary = aRoomListRoomSummary())
        setSafeContent {
            RoomListDeclineInviteMenu(
                menu = menu,
                canReportRoom = false,
                onDeclineAndBlockClick = EnsureNeverCalledWithParam(),
                eventSink = eventsRecorder,
            )
        }
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertList(listOf(RoomListEvent.HideDeclineInviteMenu))
    }
}
