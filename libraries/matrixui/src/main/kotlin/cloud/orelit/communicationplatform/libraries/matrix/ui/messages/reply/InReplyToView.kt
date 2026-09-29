/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.core.extensions.toSafeLength
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.PlaceholderAtom
import cloud.orelit.communicationplatform.libraries.designsystem.icons.CompoundDrawables
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ProfileDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.AttachmentThumbnail
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentErrorView
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.InvalidContentView
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NotFoundContentView
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.sender.SenderName
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.sender.SenderNameMode
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

/**
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2019-6286
 */
@Composable
fun InReplyToView(
    inReplyTo: InReplyToDetails,
    hideImage: Boolean,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    when (inReplyTo) {
        is InReplyToDetails.Ready -> when (contentValidationValue) {
            ContentValidationValue.Valid -> ReplyToReadyContent(
                senderId = inReplyTo.senderId,
                senderProfile = inReplyTo.senderProfile,
                metadata = inReplyTo.metadata(hideImage),
                maxLines = maxLines,
                modifier = modifier,
            )
            ContentValidationValue.Invalid -> ReplyToInvalidContent()
            is ContentValidationValue.UnrecoverableError -> ReplyToNotFoundContent()
            else -> ReplyToLoadingContent(modifier = modifier)
        }
        is InReplyToDetails.Error ->
            ReplyToErrorContent(modifier = modifier)
        is InReplyToDetails.Loading ->
            ReplyToLoadingContent(modifier = modifier)
    }
}

@Composable
private fun ReplyToReadyContent(
    senderId: UserId,
    senderProfile: ProfileDetails,
    metadata: InReplyToMetadata?,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    val paddings = if (metadata is InReplyToMetadata.Thumbnail) {
        PaddingValues(end = 8.dp)
    } else {
        PaddingValues(horizontal = 8.dp)
    }
    Row(
        modifier
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(paddings)
    ) {
        if (metadata is InReplyToMetadata.Thumbnail) {
            AttachmentThumbnail(
                info = metadata.attachmentThumbnailInfo,
                backgroundColor = ElementTheme.colors.bgSubtlePrimary,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        val a11InReplyToText = stringResource(CommonStrings.common_in_reply_to, senderProfile.getDisambiguatedDisplayName(senderId))
        Column(
            modifier = Modifier.semantics(mergeDescendants = false) { isTraversalGroup = true },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SenderName(
                senderId = senderId,
                senderProfile = senderProfile,
                senderNameMode = SenderNameMode.Reply,
                modifier = Modifier.semantics {
                    contentDescription = a11InReplyToText
                    isTraversalGroup = true
                    traversalIndex = 1f
                },
            )
            ReplyToContentText(metadata, maxLines)
        }
    }
}

@Composable
private fun ReplyToLoadingContent(
    modifier: Modifier = Modifier,
) {
    val paddings = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    Row(
        modifier
            .background(ElementTheme.colors.bgCanvasDefault)
            .padding(paddings)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PlaceholderAtom(width = 80.dp, height = 12.dp)
            PlaceholderAtom(width = 140.dp, height = 14.dp)
        }
    }
}

@Composable
private fun ReplyToErrorContent(
    modifier: Modifier = Modifier,
) {
    ContentErrorView(
        title = stringResource(CommonStrings.error_message_not_found),
        message = null,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
        onTextLayout = null,
    )
}

@Composable
private fun ReplyToContentText(
    metadata: InReplyToMetadata?,
    maxLines: Int,
) {
    val text = when (metadata) {
        InReplyToMetadata.Redacted -> stringResource(id = CommonStrings.common_message_removed)
        InReplyToMetadata.UnableToDecrypt -> stringResource(id = CommonStrings.common_waiting_for_decryption_key)
        // Add a limit to the text length to avoid a crash in Compose
        is InReplyToMetadata.Text -> metadata.text.toSafeLength()
        // Add a limit to the text length to avoid a crash in Compose
        is InReplyToMetadata.Thumbnail -> metadata.text.toSafeLength()
        null -> ""
    }
    val iconResourceId = when (metadata) {
        InReplyToMetadata.Redacted -> CompoundDrawables.ic_compound_delete
        InReplyToMetadata.UnableToDecrypt -> CompoundDrawables.ic_compound_time
        else -> null
    }
    val fontStyle = when (metadata) {
        is InReplyToMetadata.Informative -> FontStyle.Italic
        else -> FontStyle.Normal
    }
    Row(
        modifier = Modifier.semantics(mergeDescendants = false) {
            isTraversalGroup = true
            traversalIndex = -1f
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconResourceId != null) {
            Icon(
                resourceId = iconResourceId,
                tint = ElementTheme.colors.iconSecondary,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyMdRegular,
            fontStyle = fontStyle,
            textAlign = TextAlign.Start,
            color = ElementTheme.colors.textSecondary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReplyToInvalidContent(
    modifier: Modifier = Modifier
) {
    InvalidContentView(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
        onTextLayout = null,
    )
}

@Composable
private fun ReplyToNotFoundContent(
    modifier: Modifier = Modifier
) {
    NotFoundContentView(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
        onTextLayout = null,
    )
}

@PreviewsDayNight
@Composable
internal fun InReplyToViewPreview(@PreviewParameter(provider = InReplyToDetailsPreviewParam::class) inReplyTo: InReplyToDetails) = ElementPreview {
    InReplyToView(
        inReplyTo = inReplyTo,
        hideImage = false,
        contentValidationValue = ContentValidationValue.Valid,
    )
}

@PreviewsDayNight
@Composable
internal fun ReplyToInvalidContentPreview() {
    ElementPreview {
        ReplyToInvalidContent(modifier = Modifier.padding(10.dp))
    }
}

@PreviewsDayNight
@Composable
internal fun ReplyToNotFoundContentPreview() {
    ElementPreview {
        ReplyToNotFoundContent(modifier = Modifier.padding(10.dp))
    }
}
