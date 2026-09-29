/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.enterprise.test.remoteconfig

import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.MapTilerConfig

fun aMapTilerConfig(
    apiKey: String = "test_api_key",
    baseUrl: String? = null,
    lightStyleId: String? = null,
    darkStyleId: String? = null,
) = MapTilerConfig(
    apiKey = apiKey,
    baseUrl = baseUrl,
    lightStyleId = lightStyleId,
    darkStyleId = darkStyleId,
)
