/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.store

import cloud.orelit.communicationplatform.libraries.push.api.history.PushHistoryItem
import kotlinx.coroutines.flow.Flow

interface PushDataStore {
    val shouldDisplayBatteryOptimizationBannerFlow: Flow<Boolean>
    val pushCounterFlow: Flow<Int>

    /**
     * Get a flow of list of [PushHistoryItem].
     */
    fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>>

    /**
     * Reset the push counter to 0, and clear the database.
     */
    suspend fun reset()
}
