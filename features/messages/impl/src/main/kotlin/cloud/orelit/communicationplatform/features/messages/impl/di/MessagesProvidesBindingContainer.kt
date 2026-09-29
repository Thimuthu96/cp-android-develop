/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.LiveTimeline
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline

@ContributesTo(RoomScope::class)
@BindingContainer
object MessagesProvidesBindingContainer {
    @Provides
    @LiveTimeline
    fun providesLiveTimeline(joinedRoom: JoinedRoom): Timeline = joinedRoom.liveTimeline
}
