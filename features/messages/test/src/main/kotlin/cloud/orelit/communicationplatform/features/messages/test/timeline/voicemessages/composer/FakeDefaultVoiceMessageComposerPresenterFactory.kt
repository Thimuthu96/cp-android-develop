/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.test.timeline.voicemessages.composer

import cloud.orelit.communicationplatform.features.messages.impl.voicemessages.composer.DefaultVoiceMessageComposerPresenter
import cloud.orelit.communicationplatform.features.messages.impl.voicemessages.composer.VoiceMessageComposerPlayer
import cloud.orelit.communicationplatform.features.messages.test.FakeMessageComposerContext
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.mediaplayer.test.FakeAudioFocus
import cloud.orelit.communicationplatform.libraries.mediaplayer.test.FakeMediaPlayer
import cloud.orelit.communicationplatform.libraries.mediaupload.api.MediaSender
import cloud.orelit.communicationplatform.libraries.mediaupload.test.FakeMediaSender
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenterFactory
import cloud.orelit.communicationplatform.libraries.voicerecorder.test.FakeVoiceRecorder
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.CoroutineScope

class FakeDefaultVoiceMessageComposerPresenterFactory(
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaSender: MediaSender = FakeMediaSender(),
) : DefaultVoiceMessageComposerPresenter.Factory {
    override fun create(timelineMode: Timeline.Mode): DefaultVoiceMessageComposerPresenter {
        return DefaultVoiceMessageComposerPresenter(
            sessionCoroutineScope = sessionCoroutineScope,
            timelineMode = timelineMode,
            voiceRecorder = FakeVoiceRecorder(),
            analyticsService = FakeAnalyticsService(),
            audioFocus = FakeAudioFocus(
                requestAudioFocusResult = { _, _ -> },
                releaseAudioFocusResult = { },
            ),
            mediaSenderFactory = { mediaSender },
            player = VoiceMessageComposerPlayer(
                mediaPlayer = FakeMediaPlayer(),
                sessionCoroutineScope = sessionCoroutineScope,
            ),
            messageComposerContext = FakeMessageComposerContext(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        )
    }
}
