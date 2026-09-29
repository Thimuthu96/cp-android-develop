/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.di

import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageState
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.aVoiceMessageState

/**
 * A fake [TimelineItemPresenterFactories] for screenshot tests.
 */
fun aFakeTimelineItemPresenterFactories() = TimelineItemPresenterFactories(
    mapOf(
        Pair(
            TimelineItemLocationContent::class,
            TimelineItemPresenterFactory<TimelineItemLocationContent, TimelineItemLocationContent> { content ->
                Presenter { content.ensureActiveLiveLocation() }
            },
        ),
        Pair(
            TimelineItemVoiceContent::class,
            TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)
