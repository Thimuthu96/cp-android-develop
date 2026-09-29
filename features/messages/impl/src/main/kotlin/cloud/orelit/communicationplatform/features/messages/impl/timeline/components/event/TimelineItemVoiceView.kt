/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContentPreviewParam
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.PlaybackSpeedButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.media.WaveformPlaybackView
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.libraries.ui.utils.a11y.isTalkbackActive
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageEvent
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageState
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageStatePreviewParam
import kotlinx.coroutines.delay

@Composable
fun TimelineItemVoiceView(
    state: VoiceMessageState,
    content: TimelineItemVoiceContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    contentValidationValue: ContentValidationValue,
    modifier: Modifier = Modifier,
) {
    fun playPause() {
        state.eventSink(VoiceMessageEvent.PlayPause)
    }

    val a11y = stringResource(CommonStrings.common_voice_message)
    val a11yActionLabel = stringResource(
        when (state.buttonType) {
            VoiceMessageState.ButtonType.Play -> CommonStrings.a11y_play
            VoiceMessageState.ButtonType.Pause -> CommonStrings.a11y_pause
            VoiceMessageState.ButtonType.Downloading -> CommonStrings.common_downloading
            VoiceMessageState.ButtonType.Retry -> CommonStrings.action_retry
            VoiceMessageState.ButtonType.Disabled -> CommonStrings.error_unknown
        }
    )
    Row(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = a11y
                if (state.buttonType == VoiceMessageState.ButtonType.Disabled) {
                    disabled()
                } else if (state.buttonType in listOf(VoiceMessageState.ButtonType.Play, VoiceMessageState.ButtonType.Pause)) {
                    onClick(label = a11yActionLabel) {
                        playPause()
                        true
                    }
                }
            }
            .onSizeChanged {
                onContentLayoutChange(
                    ContentAvoidingLayoutData(
                        contentWidth = it.width,
                        contentHeight = it.height,
                    )
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isTalkbackActive()) {
            if (contentValidationValue.isValid()) {
                when (state.buttonType) {
                    VoiceMessageState.ButtonType.Play -> PlayButton(onClick = ::playPause)
                    VoiceMessageState.ButtonType.Pause -> PauseButton(onClick = ::playPause)
                    VoiceMessageState.ButtonType.Downloading -> ProgressButton()
                    VoiceMessageState.ButtonType.Retry -> RetryButton(onClick = ::playPause)
                    VoiceMessageState.ButtonType.Disabled -> PlayButton(onClick = {}, enabled = false)
                }
            } else {
                ProgressButton(displayImmediately = true)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            PlaybackSpeedButton(
                speed = state.playbackSpeed,
                onClick = { state.eventSink(VoiceMessageEvent.ChangePlaybackSpeed) },
            )
            Text(
                text = state.time,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        WaveformPlaybackView(
            showCursor = state.showCursor,
            playbackProgress = state.progress,
            waveform = content.waveform,
            modifier = Modifier
                .weight(1f)
                .height(34.dp),
            seekEnabled = !isTalkbackActive(),
            onSeek = { state.eventSink(VoiceMessageEvent.Seek(it)) },
        )
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    CustomIconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PlaySolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_play),
        )
    }
}

@Composable
private fun PauseButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PauseSolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_pause),
        )
    }
}

@Composable
private fun RetryButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.Restart(),
            contentDescription = stringResource(id = CommonStrings.action_retry),
        )
    }
}

@Composable
private fun ControlIcon(
    imageVector: ImageVector,
    contentDescription: String?,
) {
    Icon(
        modifier = Modifier.padding(vertical = 10.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}

/**
 * Progress button is shown when the voice message is being downloaded.
 *
 * The progress indicator is optimistic and displays a pause button (which
 * indicates the audio is playing) for 2 seconds before revealing the
 * actual progress indicator.
 */
@Composable
private fun ProgressButton(
    displayImmediately: Boolean = false,
) {
    var canDisplay by remember { mutableStateOf(displayImmediately) }
    LaunchedEffect(Unit) {
        delay(2000L)
        canDisplay = true
    }
    CustomIconButton(
        onClick = {},
        enabled = false,
    ) {
        if (canDisplay) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp),
                color = ElementTheme.colors.iconSecondary,
                strokeWidth = 2.dp,
            )
        } else {
            ControlIcon(
                imageVector = CompoundIcons.PauseSolid(),
                contentDescription = stringResource(id = CommonStrings.a11y_pause),
            )
        }
    }
}

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .size(36.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ElementTheme.colors.iconSecondary,
            disabledContentColor = ElementTheme.colors.iconDisabled,
        ),
        content = content,
    )
}

open class TimelineItemVoiceViewParametersPreviewParam : PreviewParameterProvider<TimelineItemVoiceViewParameters> {
    private val voiceMessageStateProvider = VoiceMessageStatePreviewParam()
    private val timelineItemVoiceContentProvider = TimelineItemVoiceContentPreviewParam()
    override val values: Sequence<TimelineItemVoiceViewParameters>
        get() = timelineItemVoiceContentProvider.values.flatMap { content ->
            voiceMessageStateProvider.values.map { state ->
                TimelineItemVoiceViewParameters(
                    state = state,
                    content = content,
                )
            }
        }
}

data class TimelineItemVoiceViewParameters(
    val state: VoiceMessageState,
    val content: TimelineItemVoiceContent,
)

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewPreview(
    @PreviewParameter(TimelineItemVoiceViewParametersPreviewParam::class) timelineItemVoiceViewParameters: TimelineItemVoiceViewParameters,
) = ElementPreview {
    TimelineItemVoiceView(
        state = timelineItemVoiceViewParameters.state,
        content = timelineItemVoiceViewParameters.content,
        onContentLayoutChange = {},
        contentValidationValue = ContentValidationValue.Valid,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewUnifiedPreview() = ElementPreview {
    val timelineItemVoiceViewParametersProvider = TimelineItemVoiceViewParametersPreviewParam()
    Column {
        timelineItemVoiceViewParametersProvider.values.forEach {
            TimelineItemVoiceView(
                state = it.state,
                content = it.content,
                onContentLayoutChange = {},
                contentValidationValue = ContentValidationValue.Valid,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ProgressButtonPreview() = ElementPreview {
    Row {
        ProgressButton(displayImmediately = true)
        ProgressButton(displayImmediately = false)
    }
}
