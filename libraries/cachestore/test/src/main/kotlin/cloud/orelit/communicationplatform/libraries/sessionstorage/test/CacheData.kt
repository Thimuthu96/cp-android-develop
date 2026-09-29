/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.sessionstorage.test

import cloud.orelit.communicationplatform.libraries.cachestore.api.CacheData

fun aCacheData(
    value: String = "aValue",
    updatedAt: Long = 0,
) = CacheData(
    value = value,
    updatedAt = updatedAt,
)
