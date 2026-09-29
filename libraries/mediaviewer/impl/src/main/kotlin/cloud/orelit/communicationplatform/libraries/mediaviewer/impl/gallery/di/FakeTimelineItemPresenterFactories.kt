/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery.di

import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.MediaItem
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.VoiceMessageState
import cloud.orelit.communicationplatform.libraries.voiceplayer.api.aVoiceMessageState

/**
 * A fake [MediaItemPresenterFactories] for screenshot tests.
 */
fun aFakeMediaItemPresenterFactories() = MediaItemPresenterFactories(
    mapOf(
        Pair(
            MediaItem.Voice::class,
            MediaItemPresenterFactory<MediaItem.Voice, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)
