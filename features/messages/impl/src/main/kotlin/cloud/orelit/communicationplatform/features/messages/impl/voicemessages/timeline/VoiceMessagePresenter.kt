/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.voicemessages.timeline

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessagePresenterFactory
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageState

@AssistedInject
class VoiceMessagePresenter(
    voiceMessagePresenterFactory: VoiceMessagePresenterFactory,
    @Assisted private val content: TimelineItemVoiceContent,
) : Presenter<VoiceMessageState> {
    @AssistedFactory
    fun interface Factory : TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> {
        override fun create(content: TimelineItemVoiceContent): VoiceMessagePresenter
    }

    private val presenter = voiceMessagePresenterFactory.createVoiceMessagePresenter(
        eventId = content.eventId,
        mediaSource = content.mediaSource,
        mimeType = content.mimeType,
        filename = content.filename,
        duration = content.duration,
    )

    @Composable
    override fun present(): VoiceMessageState {
        return presenter.present()
    }
}
