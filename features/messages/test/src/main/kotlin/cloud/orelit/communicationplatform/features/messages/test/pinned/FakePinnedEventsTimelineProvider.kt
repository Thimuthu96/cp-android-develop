/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.test.pinned

import cloud.orelit.communicationplatform.features.messages.api.pinned.PinnedEventsTimelineProvider
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.FakeTimelineProvider
import kotlinx.coroutines.flow.StateFlow

class FakePinnedEventsTimelineProvider(
    private val fakeTimelineProvider: FakeTimelineProvider = FakeTimelineProvider(),
) : PinnedEventsTimelineProvider {
    override fun activeTimelineFlow(): StateFlow<Timeline?> = fakeTimelineProvider.activeTimelineFlow()
}
