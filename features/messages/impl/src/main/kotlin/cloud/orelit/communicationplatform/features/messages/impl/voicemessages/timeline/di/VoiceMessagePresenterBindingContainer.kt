/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.voicemessages.timeline.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.TimelineItemEventContentKey
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.TimelineItemPresenterFactory
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import cloud.orelit.communicationplatform.features.messages.impl.voicemessages.timeline.VoiceMessagePresenter
import cloud.orelit.communicationplatform.libraries.di.RoomScope

@BindingContainer
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterBindingContainer {
    @Binds
    @IntoMap
    @TimelineItemEventContentKey(TimelineItemVoiceContent::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): TimelineItemPresenterFactory<*, *>
}
