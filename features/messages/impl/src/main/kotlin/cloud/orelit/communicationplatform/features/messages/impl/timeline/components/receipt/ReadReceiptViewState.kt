/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt

import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.ReadReceiptData
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.ImmutableList

data class ReadReceiptViewState(
    val sendState: LocalEventSendState?,
    val isLastOutgoingMessage: Boolean,
    val receipts: ImmutableList<ReadReceiptData>,
)
