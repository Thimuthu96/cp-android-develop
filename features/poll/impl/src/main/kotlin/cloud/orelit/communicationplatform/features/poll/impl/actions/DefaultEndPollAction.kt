/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.poll.impl.actions

import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.PollEnd
import cloud.orelit.communicationplatform.features.poll.api.actions.EndPollAction
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesBinding(RoomScope::class)
class DefaultEndPollAction(
    private val analyticsService: AnalyticsService,
) : EndPollAction {
    override suspend fun execute(timeline: Timeline, pollStartId: EventId): Result<Unit> {
        return timeline.endPoll(
            pollStartId = pollStartId,
            text = "The poll with event id: $pollStartId has ended."
        ).onSuccess {
            analyticsService.capture(PollEnd())
        }
    }
}
