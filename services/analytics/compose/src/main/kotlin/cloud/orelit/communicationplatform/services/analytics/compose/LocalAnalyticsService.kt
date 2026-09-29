/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.compose

import androidx.compose.runtime.staticCompositionLocalOf
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.noop.NoopAnalyticsService

/**
 * Global key to access the [AnalyticsService] in the composition tree.
 */
val LocalAnalyticsService = staticCompositionLocalOf<AnalyticsService> {
    NoopAnalyticsService()
}
