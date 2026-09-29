/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.securityandprivacy.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSectionHeader
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

// Figma design: https://www.figma.com/design/kcnHxunG1LDWXsJhaNuiHz/ER-145--Spaces-on-Element-X?node-id=6361-86274&m=dev
@Composable
fun ManageAuthorizedSpacesView(
    state: ManageAuthorizedSpacesState,
    modifier: Modifier = Modifier,
) {
    fun onCancel() {
        state.eventSink(ManageAuthorizedSpacesEvent.Cancel)
    }

    fun onDone() {
        state.eventSink(ManageAuthorizedSpacesEvent.Done)
    }

    BackHandler(onBack = ::onCancel)

    Scaffold(
        modifier = modifier,
        topBar = {
            ManageAuthorizedSpacesTopBar(
                onBackClick = ::onCancel,
                onDoneClick = ::onDone,
                isDoneButtonEnabled = state.isDoneButtonEnabled
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            headerItem()
            item {
                ListSectionHeader(
                    title = stringResource(R.string.screen_manage_authorized_spaces_your_spaces_section_title),
                    hasDivider = false,
                )
            }
            items(items = state.selectableSpaces.toList()) { space ->
                CheckableSpaceListItem(
                    headlineText = space.displayName,
                    supportingText = space.canonicalAlias?.value,
                    avatarData = space.getAvatarData(AvatarSize.SpaceMember),
                    checked = state.selectedIds.contains(space.roomId),
                    onCheckedChange = { _ ->
                        state.eventSink(
                            ManageAuthorizedSpacesEvent.ToggleSpace(space.roomId)
                        )
                    }
                )
            }
            if (state.unknownSpaceIds.isNotEmpty()) {
                item {
                    ListSectionHeader(
                        title = stringResource(R.string.screen_manage_authorized_spaces_unknown_spaces_section_title),
                        hasDivider = true,
                    )
                }
                items(items = state.unknownSpaceIds) {
                    CheckableSpaceListItem(
                        headlineText = stringResource(R.string.screen_manage_authorized_spaces_unknown_space),
                        supportingText = it.value,
                        avatarData = null,
                        checked = state.selectedIds.contains(it),
                        onCheckedChange = { _ ->
                            state.eventSink(
                                ManageAuthorizedSpacesEvent.ToggleSpace(it)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun LazyListScope.headerItem() {
    item(key = "header", contentType = "header") {
        IconTitleSubtitleMolecule(
            modifier = Modifier.padding(
                vertical = 16.dp,
                horizontal = 24.dp
            ),
            title = stringResource(R.string.screen_manage_authorized_spaces_header),
            subTitle = null,
            iconStyle = BigIcon.Style.Default(
                vectorIcon = CompoundIcons.SpaceSolid(),
            )
        )
    }
}

@Composable
private fun CheckableSpaceListItem(
    headlineText: String,
    supportingText: String?,
    avatarData: AvatarData?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ListItem(
        content = {
            Text(text = headlineText)
        },
        supportingContent = supportingText?.let {
            @Composable {
                Text(text = supportingText)
            }
        },
        leadingContent = avatarData?.let {
            ListItemContent.Custom {
                Avatar(
                    avatarData = avatarData,
                    avatarType = AvatarType.Space(),
                )
            }
        },
        trailingContent = ListItemContent.Checkbox(
            checked = checked,
            enabled = enabled,
        ),
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageAuthorizedSpacesTopBar(
    isDoneButtonEnabled: Boolean,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        titleStr = stringResource(R.string.screen_manage_authorized_spaces_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                enabled = isDoneButtonEnabled,
                text = stringResource(CommonStrings.action_done),
                onClick = onDoneClick,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ManageAuthorizedSpacesViewPreview(
    @PreviewParameter(ManageAuthorizedSpacesStatePreviewParam::class) state: ManageAuthorizedSpacesState
) = ElementPreview {
    ManageAuthorizedSpacesView(state = state)
}
