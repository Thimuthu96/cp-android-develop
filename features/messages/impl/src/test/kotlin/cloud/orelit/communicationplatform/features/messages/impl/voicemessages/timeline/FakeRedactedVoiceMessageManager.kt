/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.voicemessages.timeline

import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem

class FakeRedactedVoiceMessageManager : RedactedVoiceMessageManager {
    private val _invocations: MutableList<List<MatrixTimelineItem>> = mutableListOf()
    val invocations: List<List<MatrixTimelineItem>>
        get() = _invocations

    override suspend fun onEachMatrixTimelineItem(timelineItems: List<MatrixTimelineItem>) {
        _invocations.add(timelineItems)
    }
}
