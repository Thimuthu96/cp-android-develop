/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.live.service

import cloud.orelit.communicationplatform.features.location.api.Location

interface LiveLocationReceiver {
    suspend fun onLocationUpdate(location: Location)
    suspend fun onUnrecoverableError()
}
