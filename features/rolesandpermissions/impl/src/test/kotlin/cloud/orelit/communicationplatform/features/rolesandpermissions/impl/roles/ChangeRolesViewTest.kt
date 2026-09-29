/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.rolesandpermissions.impl.roles

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBarResultState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.toMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUserList
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.pressBackKey
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.robolectric.annotation.Config

class ChangeRolesViewTest : RobolectricTest() {
    @Test
    fun `passing a 'User' role throws an exception`() = runAndroidComposeUiTest {
        val exception = runCatchingExceptions {
            setChangeRolesContent(
                state = aChangeRolesState(
                    role = RoomMember.Role.User,
                    eventSink = EnsureNeverCalledWithParam(),
                ),
            )
        }.exceptionOrNull()
        assertThat(exception).isNotNull()
    }

    @Test
    fun `back key - with search active toggles the search`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )
        pressBackKey()

        // Advance time to let the event be processed, as the search toggle might have some delay (e.g. for the animation)
        mainClock.advanceTimeBy(1)

        eventsRecorder.assertSingle(ChangeRolesEvent.ToggleSearchActive)
    }

    @Test
    fun `back key - with search inactive exits the screen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = false,
                eventSink = eventsRecorder,
            ),
        )
        pressBackKey()
        eventsRecorder.assertSingle(ChangeRolesEvent.Exit)
    }

    @Test
    fun `back button - exits the screen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = false,
                eventSink = eventsRecorder,
            ),
        )
        pressBack()
        eventsRecorder.assertSingle(ChangeRolesEvent.Exit)
    }

    @Test
    fun `save button - with changes, it saves them`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                hasPendingChanges = true,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save button - with no changes, does nothing`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                hasPendingChanges = false,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_save)
        eventsRecorder.assertEmpty()
    }

    @Test
    fun `exit confirmation dialog - discard exits the screen`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                savingState = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_discard)
        eventsRecorder.assertSingle(ChangeRolesEvent.Exit)
    }

    @Test
    fun `exit confirmation dialog - save emits the save event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                savingState = AsyncAction.ConfirmingCancellation,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_save)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save admins confirmation dialog - submit saves the changes`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Admin,
                isSearchActive = true,
                savingState = ConfirmingModifyingAdmins,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save owners confirmation dialog - continue saves the changes`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Owner(isCreator = false),
                isSearchActive = true,
                savingState = ConfirmingModifyingOwners,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ChangeRolesEvent.Save)
    }

    @Test
    fun `save admins confirmation dialog - cancel removes the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Admin,
                isSearchActive = true,
                savingState = ConfirmingModifyingAdmins,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `save owners confirmation dialog - cancel removes the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                role = RoomMember.Role.Owner(isCreator = false),
                isSearchActive = true,
                savingState = ConfirmingModifyingOwners,
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `error dialog - dismissing removes the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        setChangeRolesContent(
            state = aChangeRolesState(
                isSearchActive = true,
                savingState = AsyncAction.Failure(IllegalStateException("boom")),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventsRecorder.assertSingle(ChangeRolesEvent.CloseDialog)
    }

    @Test
    fun `testing removing user from selected list emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val userToDeselect = selectedUsers[1]
        assertThat(userToDeselect.displayName).isEqualTo("Bob")
        setChangeRolesContent(
            state = aChangeRolesStateWithSelectedUsers().copy(
                selectedUsers = selectedUsers.toImmutableList(),
                eventSink = eventsRecorder,
            ),
        )
        // Unselect the user from the row list
        val contentDescription = activity!!.getString(CommonStrings.action_remove)
        onNodeWithContentDescription(
            label = contentDescription,
            useUnmergedTree = true,
        ).performClick()
        eventsRecorder.assertSingle(ChangeRolesEvent.UserSelectionToggled(userToDeselect))
    }

    @Test
    @Config(qualifiers = "h1000dp")
    fun `testing adding user to the selected list emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val state = aChangeRolesStateWithSelectedUsers().copy(
            selectedUsers = selectedUsers.toImmutableList(),
            eventSink = eventsRecorder,
        )
        val userToSelect = (state.searchResults as SearchBarResultState.Results).results.members.first().toMatrixUser()
        assertThat(userToSelect.displayName).isEqualTo("Carol")
        setChangeRolesContent(
            state = state,
        )
        // Select the user from the user list
        onNodeWithText("Carol").performClick()
        eventsRecorder.assertSingle(ChangeRolesEvent.UserSelectionToggled(userToSelect))
    }

    @Test
    fun `testing removing user to the selected list emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ChangeRolesEvent>()
        val selectedUsers = aMatrixUserList().take(2)
        val state = aChangeRolesStateWithSelectedUsers().copy(
            selectedUsers = selectedUsers.toImmutableList(),
            eventSink = eventsRecorder,
        )
        val userToSelect = (state.searchResults as SearchBarResultState.Results).results.moderators.first().toMatrixUser()
        assertThat(userToSelect.displayName).isEqualTo("Bob")
        setChangeRolesContent(
            state = state,
        )
        // Unselect the user from the user list
        onAllNodesWithText(
            text = "Bob",
            useUnmergedTree = true,
        )[1].performClick()
        eventsRecorder.assertSingle(ChangeRolesEvent.UserSelectionToggled(userToSelect))
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setChangeRolesContent(
        state: ChangeRolesState,
    ) {
        setContent {
            ChangeRolesView(
                state = state,
            )
        }
    }
}
