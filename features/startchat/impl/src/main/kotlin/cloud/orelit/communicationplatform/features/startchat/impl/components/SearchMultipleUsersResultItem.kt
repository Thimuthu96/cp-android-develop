/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRow
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRowData
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult

@Composable
fun SearchMultipleUsersResultItem(
    searchResult: UserSearchResult,
    isUserSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = if (searchResult.isUnresolved) {
        CheckableUserRowData.Unresolved(
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
            id = searchResult.matrixUser.userId.value,
        )
    } else {
        CheckableUserRowData.Resolved(
            name = searchResult.matrixUser.getBestName(),
            subtext = if (searchResult.matrixUser.displayName.isNullOrEmpty()) null else searchResult.matrixUser.userId.value,
            avatarData = searchResult.matrixUser.getAvatarData(AvatarSize.UserListItem),
        )
    }
    CheckableUserRow(
        checked = isUserSelected,
        modifier = modifier,
        data = data,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Composable
internal fun SearchMultipleUsersResultItemPreview() = ElementThemedPreview {
    Column {
        SearchMultipleUsersResultItem(
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = false
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = false,
            onCheckedChange = {}
        )
        HorizontalDivider()
        SearchMultipleUsersResultItem(
            searchResult = UserSearchResult(
                aMatrixUser(),
                isUnresolved = true
            ),
            isUserSelected = true,
            onCheckedChange = {}
        )
    }
}
