/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.features.messages.impl.timeline.aTimelineItemEvent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.aTimelineItemReactions
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItemGroupPosition
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UtdCause

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowUtdPreview() = ElementPreview {
    Column {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = USER_NAME_ALICE,
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.UnsignedDevice,
                    )
                ),
                timelineItemReactions = aTimelineItemReactions(count = 0),
                groupPosition = TimelineItemGroupPosition.First,
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = USER_NAME_BOB,
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.VerificationViolation,
                    )
                ),
                groupPosition = TimelineItemGroupPosition.First,
                timelineItemReactions = aTimelineItemReactions(count = 0)
            ),
        )

        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = USER_NAME_BOB,
                isMine = false,
                content = TimelineItemEncryptedContent(
                    data = UnableToDecryptContent.Data.MegolmV1AesSha2(
                        sessionId = "sessionId",
                        utdCause = UtdCause.SentBeforeWeJoined,
                    )
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                timelineItemReactions = aTimelineItemReactions(count = 0)
            ),
        )
    }
}
