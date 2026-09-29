/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.enterprise.impl.remoteconfig

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.CustomMapTilerConfigProvider
import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.MapTilerConfig
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultCustomMapTilerConfigProvider : CustomMapTilerConfigProvider {
    override suspend fun get(): Result<MapTilerConfig?> = Result.success(null)
}
