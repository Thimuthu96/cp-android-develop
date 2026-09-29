/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes
import cloud.orelit.communicationplatform.libraries.designsystem.colors.AvatarColorsProvider
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItemStyle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ModalBottomSheet
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.R
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.libraries.ui.strings.Strings

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=2229-149220
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailsBottomSheet(
    state: MediaBottomSheetState.Details,
    onViewInTimeline: (EventId) -> Unit,
    onShare: (EventId) -> Unit,
    onForward: (EventId) -> Unit,
    onDownload: (EventId) -> Unit,
    onOpenWith: (EventId) -> Unit,
    onDelete: (EventId) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        scrollable = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Title()
            Section(
                title = stringResource(R.string.screen_media_details_uploaded_by),
            ) {
                SenderRow(
                    mediaInfo = state.mediaInfo,
                )
            }
            SectionText(
                title = stringResource(R.string.screen_media_details_uploaded_on),
                text = state.mediaInfo.dateSentFull.orEmpty(),
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_filename),
                text = state.mediaInfo.filename,
            )
            SectionText(
                title = stringResource(R.string.screen_media_details_file_format),
                text = state.mediaInfo.mimeType + Strings.NICE_SEPARATOR + state.mediaInfo.formattedFileSize,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.eventId != null) {
                HorizontalDivider()
                val mimeType = state.mediaInfo.mimeType
                val icon = when (mimeType) {
                    MimeTypes.Apk ->
                        ListItemContent.Icon(IconSource.Resource(R.drawable.ic_apk_install))
                    else ->
                        ListItemContent.Icon(IconSource.Vector(CompoundIcons.PopOut()))
                }
                val wording = when (mimeType) {
                    MimeTypes.Apk -> stringResource(id = CommonStrings.common_install_apk_android)
                    else -> stringResource(id = CommonStrings.action_open_with)
                }
                ListItem(
                    leadingContent = icon,
                    content = { Text(wording) },
                    onClick = {
                        onOpenWith(state.eventId)
                    }
                )
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.VisibilityOn())),
                    content = { Text(stringResource(CommonStrings.action_view_in_timeline)) },
                    onClick = {
                        onViewInTimeline(state.eventId)
                    }
                )
                if (state.fromGallery) {
                    ListItem(
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ShareAndroid())),
                        content = { Text(stringResource(CommonStrings.action_share)) },
                        onClick = {
                            onShare(state.eventId)
                        }
                    )
                    ListItem(
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Download())),
                        content = { Text(stringResource(CommonStrings.action_download)) },
                        onClick = {
                            onDownload(state.eventId)
                        }
                    )
                }
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Forward())),
                    content = { Text(stringResource(CommonStrings.action_forward)) },
                    onClick = {
                        onForward(state.eventId)
                    }
                )
                if (state.canDelete) {
                    HorizontalDivider()
                    ListItem(
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Delete())),
                        content = { Text(stringResource(CommonStrings.action_delete_file)) },
                        style = ListItemStyle.Destructive,
                        onClick = {
                            onDelete(state.eventId)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SenderRow(
    mediaInfo: MediaInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val id = mediaInfo.senderId?.value ?: "@Alice:domain"
        Avatar(
            avatarData = AvatarData(
                id = id,
                name = mediaInfo.senderName,
                url = mediaInfo.senderAvatar,
                size = AvatarSize.MediaSender,
            ),
            avatarType = AvatarType.User,
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
        ) {
            // Name
            val bestName = mediaInfo.senderName ?: mediaInfo.senderId?.value.orEmpty()
            val avatarColors = AvatarColorsProvider.provide(id)
            Text(
                modifier = Modifier.clipToBounds(),
                text = bestName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = avatarColors.foreground,
                style = ElementTheme.typography.fontBodyMdMedium,
            )
            // Id
            if (!mediaInfo.senderName.isNullOrEmpty()) {
                Text(
                    text = mediaInfo.senderId?.value.orEmpty(),
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
            }
        }
    }
}

@Composable
private fun Title() {
    Text(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            .semantics {
                heading()
            },
        text = stringResource(R.string.screen_media_details_title),
        style = ElementTheme.typography.fontBodyLgMedium,
        color = ElementTheme.colors.textPrimary,
    )
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textSecondary,
        )
        content()
    }
}

@Composable
private fun SectionText(
    title: String,
    text: String,
) {
    Section(title = title) {
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MediaDetailsBottomSheetPreview(
    @PreviewParameter(MediaBottomSheetStateDetailsPreviewParam::class) state: MediaBottomSheetState.Details,
) = ElementPreview(fillMaxSize = true) {
    MediaDetailsBottomSheet(
        state = state,
        onViewInTimeline = {},
        onShare = {},
        onForward = {},
        onDownload = {},
        onOpenWith = {},
        onDelete = {},
        onDismiss = {},
    )
}
