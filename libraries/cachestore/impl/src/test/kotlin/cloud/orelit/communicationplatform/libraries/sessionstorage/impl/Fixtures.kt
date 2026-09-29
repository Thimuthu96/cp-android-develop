/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.sessionstorage.impl

import cloud.orelit.communicationplatform.libraries.cachestore.CacheData
import java.util.Date

internal fun aCacheData(
    key: String = "aKey",
    value: String = "aValue",
    updatedAt: Date = Date(),
) = CacheData(
    key = key,
    value_ = value,
    updatedAt = updatedAt.time,
)
