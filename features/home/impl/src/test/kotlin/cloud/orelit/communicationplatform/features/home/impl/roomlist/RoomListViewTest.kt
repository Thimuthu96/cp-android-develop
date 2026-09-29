/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.home.impl.roomlist

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.home.impl.HomeView
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.features.home.impl.aHomeState
import cloud.orelit.communicationplatform.features.home.impl.components.RoomListMenuAction
import cloud.orelit.communicationplatform.features.home.impl.model.RoomListRoomSummary
import cloud.orelit.communicationplatform.features.home.impl.model.RoomSummaryDisplayType
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.setSafeContent
import org.junit.Test
import org.robolectric.annotation.Config

class RoomListViewTest : RobolectricTest() {
    @Config(qualifiers = "h1024dp")
    @Test
    fun `displaying the view automatically sends a couple of UpdateVisibleRangeEvents`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                eventSink = eventsRecorder,
            )
        )

        eventsRecorder.assertList(
            listOf(
                RoomListEvent.UpdateVisibleRange(0..5),
            )
        )
    }

    @Test
    fun `clicking on close recovery key banner emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                eventSink = eventsRecorder,
            )
        )

        // Remove automatic initial events
        eventsRecorder.clear()

        val close = activity!!.getString(CommonStrings.action_close)
        onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvent.DismissBanner)
    }

    @Test
    fun `clicking on close setup key banner emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.SetUpRecovery),
                eventSink = eventsRecorder,
            )
        )

        // Remove automatic initial events
        eventsRecorder.clear()

        val close = activity!!.getString(CommonStrings.action_close)
        onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvent.DismissBanner)
    }

    @Test
    fun `clicking on continue recovery key banner invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        ensureCalledOnce { callback ->
            setRoomListView(
                state = aRoomListState(
                    contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                    eventSink = eventsRecorder,
                ),
                onConfirmRecoveryKeyClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            clickOn(CommonStrings.action_continue)

            eventsRecorder.assertEmpty()
        }
    }

    @Test
    fun `clicking on continue setup key banner invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        ensureCalledOnce { callback ->
            setRoomListView(
                state = aRoomListState(
                    contentState = aRoomsContentState(securityBannerState = SecurityBannerState.SetUpRecovery),
                    eventSink = eventsRecorder,
                ),
                onSetUpRecoveryClick = callback,
            )
            // Remove automatic initial events
            eventsRecorder.clear()
            clickOn(R.string.banner_set_up_recovery_submit)
            eventsRecorder.assertEmpty()
        }
    }

    @Test
    fun `clicking on start chat when the session has no room invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setRoomListView(
                state = aRoomListState(
                    eventSink = eventsRecorder,
                    contentState = anEmptyContentState(),
                ),
                onCreateRoomClick = callback,
            )
            clickOn(CommonStrings.action_start_chat)
        }
    }

    @Test
    fun `clicking on a room invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        ensureCalledOnceWithParam(room0.roomId) { callback ->
            setRoomListView(
                state = state,
                onRoomClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            onNodeWithText(room0.latestEvent.content().toString()).performClick()
        }

        eventsRecorder.assertEmpty()
    }

    @Test
    fun `clicking on a room twice invokes the expected callback only once`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        ensureCalledOnceWithParam(room0.roomId) { callback ->
            setRoomListView(
                state = state,
                onRoomClick = callback,
            )
            // Remove automatic initial events
            eventsRecorder.clear()
            onNodeWithText(room0.latestEvent.content().toString())
                .performClick()
                .performClick()
        }
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `long clicking on a room emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        setRoomListView(
            state = state,
        )
        // Remove automatic initial events
        eventsRecorder.clear()

        onNodeWithText(room0.latestEvent.content().toString()).performTouchInput { longClick() }
        eventsRecorder.assertSingle(RoomListEvent.ShowContextMenu(room0))
    }

    @Test
    fun `clicking on a room setting invokes the expected callback and emits expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val state = aRoomListState(
            contextMenu = aContextMenuShown(),
            eventSink = eventsRecorder,
        )
        val room0 = (state.contextMenu as RoomListState.ContextMenu.Shown).roomId
        ensureCalledOnceWithParam(room0) { callback ->
            setRoomListView(
                state = state,
                onRoomSettingsClick = callback,
            )

            // Remove automatic initial events
            eventsRecorder.clear()

            clickOn(CommonStrings.common_settings)
        }

        eventsRecorder.assertSingle(RoomListEvent.HideContextMenu)
    }

    @Test
    fun `clicking on accept and decline invite emits the expected Events`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomListEvent>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val invitedRoom = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.INVITE
        }
        setRoomListView(state = state)

        // Remove automatic initial events
        eventsRecorder.clear()

        clickOn(CommonStrings.action_accept)
        clickOn(CommonStrings.action_decline)
        eventsRecorder.assertList(
            listOf(
                RoomListEvent.AcceptInvite(invitedRoom),
                RoomListEvent.ShowDeclineInviteMenu(invitedRoom),
            )
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomListView(
    state: RoomListState,
    onRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onSettingsClick: () -> Unit = EnsureNeverCalled(),
    onSetUpRecoveryClick: () -> Unit = EnsureNeverCalled(),
    onConfirmRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    onCreateRoomClick: () -> Unit = EnsureNeverCalled(),
    onCreateSpaceClick: () -> Unit = EnsureNeverCalled(),
    onRoomSettingsClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onMenuActionClick: (RoomListMenuAction) -> Unit = EnsureNeverCalledWithParam(),
    onReportRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onDeclineInviteAndBlockUser: (RoomListRoomSummary) -> Unit = EnsureNeverCalledWithParam(),
) {
    setSafeContent {
        HomeView(
            homeState = aHomeState(roomListState = state),
            onRoomClick = { roomId, _ -> onRoomClick(roomId) },
            onSettingsClick = onSettingsClick,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onStartChatClick = onCreateRoomClick,
            onCreateSpaceClick = onCreateSpaceClick,
            onRoomSettingsClick = onRoomSettingsClick,
            onMenuActionClick = onMenuActionClick,
            onDeclineInviteAndBlockUser = onDeclineInviteAndBlockUser,
            onReportRoomClick = onReportRoomClick,
            acceptDeclineInviteView = {},
            leaveRoomView = {},
        )
    }
}
