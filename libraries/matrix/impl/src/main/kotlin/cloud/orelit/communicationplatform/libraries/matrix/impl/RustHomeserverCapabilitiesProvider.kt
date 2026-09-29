/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl

import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.matrix.api.HomeserverCapabilitiesProvider
import org.matrix.rustcomponents.sdk.HomeserverCapabilities

class RustHomeserverCapabilitiesProvider(
    private val homeserverCapabilities: HomeserverCapabilities,
) : HomeserverCapabilitiesProvider {
    override suspend fun refresh(): Result<Unit> = runCatchingExceptions {
        homeserverCapabilities.refresh()
    }

    override suspend fun canChangeDisplayName(): Result<Boolean> = runCatchingExceptions {
        homeserverCapabilities.canChangeDisplayname()
    }

    override suspend fun canChangeAvatarUrl(): Result<Boolean> = runCatchingExceptions {
        homeserverCapabilities.canChangeAvatar()
    }
}
