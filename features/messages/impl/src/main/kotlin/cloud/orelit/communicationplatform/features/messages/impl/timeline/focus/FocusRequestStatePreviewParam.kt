/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.focus

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.messages.impl.timeline.FocusRequestState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.errors.FocusEventException

open class FocusRequestStatePreviewParam : PreviewParameterProvider<FocusRequestState> {
    override val values: Sequence<FocusRequestState>
        get() = sequenceOf(
            FocusRequestState.Loading(
                eventId = EventId("\$anEventId"),
            ),
            FocusRequestState.Failure(
                FocusEventException.EventNotFound(
                    eventId = EventId("\$anEventId"),
                )
            ),
            FocusRequestState.Failure(
                FocusEventException.InvalidEventId(
                    eventId = "invalid",
                    err = "An error"
                )
            ),
            FocusRequestState.Failure(
                FocusEventException.Other(
                    msg = "An error"
                )
            ),
        )
}
