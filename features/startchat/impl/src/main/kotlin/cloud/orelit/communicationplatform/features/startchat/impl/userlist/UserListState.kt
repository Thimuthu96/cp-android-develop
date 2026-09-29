/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.userlist

import androidx.compose.foundation.text.input.TextFieldState
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBarResultState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.recent.RecentDirectRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList

data class UserListState(
    val searchQuery: TextFieldState,
    val searchResults: SearchBarResultState<ImmutableList<UserSearchResult>>,
    val showSearchLoader: Boolean,
    val selectedUsers: ImmutableList<MatrixUser>,
    val isSearchActive: Boolean,
    val selectionMode: SelectionMode,
    val recentDirectRooms: ImmutableList<RecentDirectRoom>,
    val eventSink: (UserListEvent) -> Unit,
) {
    val isMultiSelectionEnabled = selectionMode == SelectionMode.Multiple
}
