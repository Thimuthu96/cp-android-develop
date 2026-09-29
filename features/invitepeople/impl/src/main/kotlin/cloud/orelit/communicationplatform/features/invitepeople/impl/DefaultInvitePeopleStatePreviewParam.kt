/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invitepeople.impl

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_CAROL
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_EVE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_JUSTIN
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBarResultState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class DefaultInvitePeopleStatePreviewParam : PreviewParameterProvider<DefaultInvitePeopleState> {
    override val values: Sequence<DefaultInvitePeopleState>
        get() = sequenceOf(
            aDefaultInvitePeopleState(),
            aDefaultInvitePeopleState(canInvite = true, selectedUsers = aMatrixUserList().toImmutableList()),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query"),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query", selectedUsers = aMatrixUserList().toImmutableList()),
            aDefaultInvitePeopleState(isSearchActive = true, searchQuery = "some query", searchResults = SearchBarResultState.NoResultsFound),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "some query",
                selectedUsers = persistentListOf(
                    aMatrixUser(displayName = USER_NAME_CAROL)
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_ALICE)),
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_BOB)),
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_CAROL), isSelected = true),
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_EVE), isSelected = true, isAlreadyJoined = true),
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_JUSTIN), isSelected = true, isAlreadyInvited = true),
                    )
                )
            ),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                selectedUsers = persistentListOf(
                    aMatrixUser(displayName = USER_NAME_CAROL)
                ),
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_ALICE), isUnresolved = true),
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_BOB)),
                    )
                )
            ),
            aDefaultInvitePeopleState(
                isSearchActive = true,
                canInvite = true,
                searchQuery = "@alice:server.org",
                searchResults = SearchBarResultState.Results(
                    persistentListOf(
                        anInvitableUser(aMatrixUser(displayName = USER_NAME_ALICE), isUnresolved = true),
                    )
                ),
                showSearchLoader = true,
            ),
            aDefaultInvitePeopleState(room = AsyncData.Failure(Exception("Room not found"))),
            aDefaultInvitePeopleState(
                canInvite = false,
                selectedUsers = aMatrixUserList().toImmutableList(),
                sendInvitesAction = AsyncAction.Loading,
            ),
            aDefaultInvitePeopleState(
                sendInvitesAction = ConfirmingUnknownUserInvitation(
                    persistentListOf(
                        aMatrixUser(),
                    )
                )
            ),
            aDefaultInvitePeopleState(
                sendInvitesAction = ConfirmingUnknownUserInvitation(
                    aMatrixUserList().toImmutableList()
                )
            ),
        )
}

private fun anInvitableUser(
    matrixUser: MatrixUser,
    isSelected: Boolean = false,
    isAlreadyJoined: Boolean = false,
    isAlreadyInvited: Boolean = false,
    isUnresolved: Boolean = false,
) = InvitableUser(
    matrixUser = matrixUser,
    isSelected = isSelected,
    isAlreadyJoined = isAlreadyJoined,
    isAlreadyInvited = isAlreadyInvited,
    isUnresolved = isUnresolved,
)

private fun aDefaultInvitePeopleState(
    room: AsyncData<Unit> = AsyncData.Success(Unit),
    canInvite: Boolean = false,
    searchQuery: String = "",
    searchResults: SearchBarResultState<ImmutableList<InvitableUser>> = SearchBarResultState.Initial,
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    isSearchActive: Boolean = false,
    showSearchLoader: Boolean = false,
    sendInvitesAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    createRoomFromDmAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    suggestions: List<InvitableUser> = aMatrixUserList()
        .take(5)
        .map { user -> anInvitableUser(matrixUser = user, isSelected = user in selectedUsers) },
): DefaultInvitePeopleState {
    return DefaultInvitePeopleState(
        room = room,
        canInvite = canInvite,
        searchQuery = TextFieldState(initialText = searchQuery),
        searchResults = searchResults,
        selectedUsers = selectedUsers,
        isSearchActive = isSearchActive,
        showSearchLoader = showSearchLoader,
        sendInvitesAction = sendInvitesAction,
        createRoomFromDmAction = createRoomFromDmAction,
        suggestions = suggestions.toImmutableList(),
        eventSink = {},
    )
}
