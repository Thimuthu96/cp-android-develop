/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.featureflag.impl

import cloud.orelit.communicationplatform.libraries.featureflag.api.Feature

interface MutableFeatureFlagProvider : FeatureFlagProvider {
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean)
}
