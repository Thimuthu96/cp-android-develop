/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.userlist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBarResultState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.recent.RecentDirectRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUserList
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

open class UserListStatePreviewParam : PreviewParameterProvider<UserListState> {
    override val values: Sequence<UserListState>
        get() = sequenceOf(
            aUserListState(),
            aUserListState(
                isSearchActive = false,
                selectedUsers = aListOfSelectedUsers(),
                selectionMode = SelectionMode.Multiple,
            ),
            aUserListState(isSearchActive = true),
            aUserListState(isSearchActive = true, searchQuery = "someone"),
            aUserListState(isSearchActive = true, searchQuery = "someone", selectionMode = SelectionMode.Multiple),
            aUserListState(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectedUsers = aMatrixUserList().toImmutableList(),
                searchResults = SearchBarResultState.Results(aListOfUserSearchResults()),
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                selectionMode = SelectionMode.Multiple,
                selectedUsers = aMatrixUserList().toImmutableList(),
                searchResults = SearchBarResultState.Results(aListOfUserSearchResults()),
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "something-with-no-results",
                searchResults = SearchBarResultState.NoResultsFound
            ),
            aUserListState(
                isSearchActive = true,
                searchQuery = "someone",
                selectionMode = SelectionMode.Single,
            ),
            aUserListState(
                recentDirectRooms = aRecentDirectRoomList(),
            ),
        )
}

fun aUserListState(
    searchQuery: String = "",
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<ImmutableList<UserSearchResult>> = SearchBarResultState.Initial,
    selectedUsers: List<MatrixUser> = emptyList(),
    showSearchLoader: Boolean = false,
    selectionMode: SelectionMode = SelectionMode.Single,
    recentDirectRooms: List<RecentDirectRoom> = emptyList(),
    eventSink: (UserListEvent) -> Unit = {},
) = UserListState(
    searchQuery = TextFieldState(initialText = searchQuery),
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    selectedUsers = selectedUsers.toImmutableList(),
    showSearchLoader = showSearchLoader,
    selectionMode = selectionMode,
    recentDirectRooms = recentDirectRooms.toImmutableList(),
    eventSink = eventSink
)

fun aListOfSelectedUsers() = aMatrixUserList().take(6).toImmutableList()
fun aListOfUserSearchResults() = aMatrixUserList().take(6).map { UserSearchResult(it) }.toImmutableList()

fun aRecentDirectRoomList(
    count: Int = 5
): List<RecentDirectRoom> = aMatrixUserList()
    .take(count)
    .map {
        RecentDirectRoom(RoomId("!aRoom:id"), it)
    }
