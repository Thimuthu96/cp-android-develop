/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId

sealed interface HomeEvent {
    data class SelectHomeNavigationBarItem(val item: HomeNavigationBarItem) : HomeEvent
    data class SwitchToAccount(val sessionId: SessionId) : HomeEvent
}
