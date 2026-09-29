/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl.history

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.push.api.history.PushHistoryItem
import kotlinx.collections.immutable.ImmutableList

data class PushHistoryState(
    val pushCounter: Int,
    val pushHistoryItems: ImmutableList<PushHistoryItem>,
    val showOnlyErrors: Boolean,
    val resetAction: AsyncAction<Unit>,
    val showNotSameAccountError: Boolean,
    val eventSink: (PushHistoryEvent) -> Unit,
)
