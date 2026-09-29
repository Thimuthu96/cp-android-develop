/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.classic

import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

sealed interface NavigationEvent {
    data object Idle : NavigationEvent
    data object NavigateToOnBoarding : NavigationEvent
    data class NavigateToLoginWithClassic(
        val userId: UserId,
    ) : NavigationEvent
}
