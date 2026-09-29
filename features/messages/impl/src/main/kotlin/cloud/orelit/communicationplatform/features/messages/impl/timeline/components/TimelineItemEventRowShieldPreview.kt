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
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItemGroupPosition
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageShield

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowShieldPreview() = ElementPreview {
    Column {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Sender with a super long name that should ellipsize",
                isMine = true,
                content = aTimelineItemTextContent(
                    body = "Message sent from unsigned device"
                ),
                groupPosition = TimelineItemGroupPosition.First,
                messageShield = aCriticalShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                senderDisplayName = "Sender with a super long name that should ellipsize",
                content = aTimelineItemTextContent(
                    body = "Short Message with authenticity warning"
                ),
                groupPosition = TimelineItemGroupPosition.Middle,
                messageShield = aWarningShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                isMine = true,
                content = aTimelineItemImageContent(
                    aspectRatio = 2.5f
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                messageShield = aCriticalShield()
            ),
        )
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                content = aTimelineItemImageContent(
                    aspectRatio = 2.5f
                ),
                groupPosition = TimelineItemGroupPosition.Last,
                messageShield = aWarningShield()
            ),
        )
    }
}

private fun aWarningShield() = MessageShield.AuthenticityNotGuaranteed(isCritical = false)

internal fun aCriticalShield() = MessageShield.UnverifiedIdentity(isCritical = true)
