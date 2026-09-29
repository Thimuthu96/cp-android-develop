/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.shared.blockuser

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEvent
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileState
import cloud.orelit.communicationplatform.features.userprofile.shared.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.core.bool.orFalse
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.RetryDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferenceCategory
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItemStyle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun BlockUserSection(
    state: UserProfileState,
    modifier: Modifier = Modifier,
) {
    val isBlocked = state.isBlocked
    PreferenceCategory(
        modifier = modifier,
        showTopDivider = false,
    ) {
        when (isBlocked) {
            is AsyncData.Failure -> PreferenceBlockUser(isBlocked = isBlocked.prevData, isLoading = false, eventSink = state.eventSink)
            is AsyncData.Loading -> PreferenceBlockUser(isBlocked = isBlocked.prevData, isLoading = true, eventSink = state.eventSink)
            is AsyncData.Success -> PreferenceBlockUser(isBlocked = isBlocked.data, isLoading = false, eventSink = state.eventSink)
            AsyncData.Uninitialized -> PreferenceBlockUser(isBlocked = null, isLoading = true, eventSink = state.eventSink)
        }
    }
    if (isBlocked is AsyncData.Failure) {
        RetryDialog(
            content = stringResource(CommonStrings.error_unknown),
            onDismiss = { state.eventSink(UserProfileEvent.ClearBlockUserError) },
            onRetry = {
                val event = when (isBlocked.prevData) {
                    true -> UserProfileEvent.UnblockUser(needsConfirmation = false)
                    false -> UserProfileEvent.BlockUser(needsConfirmation = false)
                    // null case Should not happen
                    null -> UserProfileEvent.ClearBlockUserError
                }
                state.eventSink(event)
            },
        )
    }
}

@Composable
private fun PreferenceBlockUser(
    isBlocked: Boolean?,
    isLoading: Boolean,
    eventSink: (UserProfileEvent) -> Unit,
) {
    val loadingCurrentValue = @Composable { _: Boolean ->
        CircularProgressIndicator(
            modifier = Modifier
                .progressSemantics()
                .size(20.dp),
            strokeWidth = 2.dp
        )
    }
    if (isBlocked.orFalse()) {
        ListItem(
            content = { Text(stringResource(R.string.screen_dm_details_unblock_user)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
            onClick = { if (!isLoading) eventSink(UserProfileEvent.UnblockUser(needsConfirmation = true)) },
            trailingContent = if (isLoading) ListItemContent.Custom(loadingCurrentValue) else null,
        )
    } else {
        ListItem(
            content = { Text(stringResource(R.string.screen_dm_details_block_user)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
            style = ListItemStyle.Destructive,
            onClick = { if (!isLoading) eventSink(UserProfileEvent.BlockUser(needsConfirmation = true)) },
            trailingContent = if (isLoading) ListItemContent.Custom(loadingCurrentValue) else null,
        )
    }
}
