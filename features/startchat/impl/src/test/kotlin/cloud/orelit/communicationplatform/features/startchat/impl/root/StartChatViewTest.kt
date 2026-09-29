/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.startchat.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.startchat.impl.R
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.aRecentDirectRoomList
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.aUserListState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class StartChatViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onCloseClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on New room invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onNewRoomClick = it
            )
            clickOn(R.string.screen_create_room_action_create_room)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Invite people invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    applicationName = "test",
                    eventSink = eventsRecorder,
                ),
                onInviteFriendsClick = it
            )
            val text = activity!!.getString(CommonStrings.action_invite_friends_to_app, "test")
            onNodeWithText(text).performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on a user suggestion invokes the expected callback`() = runAndroidComposeUiTest {
        val recentDirectRoomList = aRecentDirectRoomList()
        val firstRoom = recentDirectRoomList[0]
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnceWithParam(firstRoom.roomId) {
            setStartChatView(
                aCreateRoomRootState(
                    userListState = aUserListState(
                        recentDirectRooms = recentDirectRoomList
                    ),
                    eventSink = eventsRecorder,
                ),
                onOpenDM = it
            )
            onNodeWithText(firstRoom.matrixUser.getBestName()).performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Join room by address invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onJoinRoomByAddressClick = it
            )
            clickOn(R.string.screen_start_chat_join_room_by_address_action)
        }
    }

    @Test
    fun `clicking on room directory invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<StartChatEvent>(expectEvents = false)
        ensureCalledOnce {
            setStartChatView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onRoomDirectorySearchClick = it
            )
            clickOn(R.string.screen_room_directory_search_title)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setStartChatView(
    state: StartChatState,
    onCloseClick: () -> Unit = EnsureNeverCalled(),
    onNewRoomClick: () -> Unit = EnsureNeverCalled(),
    onOpenDM: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onInviteFriendsClick: () -> Unit = EnsureNeverCalled(),
    onJoinRoomByAddressClick: () -> Unit = EnsureNeverCalled(),
    onRoomDirectorySearchClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        StartChatView(
            state = state,
            onCloseClick = onCloseClick,
            onNewRoomClick = onNewRoomClick,
            onOpenDM = onOpenDM,
            onInviteFriendsClick = onInviteFriendsClick,
            onJoinByAddressClick = onJoinRoomByAddressClick,
            onRoomDirectorySearchClick = onRoomDirectorySearchClick,
        )
    }
}
