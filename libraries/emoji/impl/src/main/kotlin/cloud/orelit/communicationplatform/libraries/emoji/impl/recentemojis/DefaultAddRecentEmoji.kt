/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.emoji.impl.recentemojis

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.emoji.api.recentemojis.AddRecentEmoji
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.withContext

@ContributesBinding(SessionScope::class)
class DefaultAddRecentEmoji(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
) : AddRecentEmoji {
    override suspend operator fun invoke(emoji: String): Result<Unit> = withContext(dispatchers.io) {
        client.addRecentEmoji(emoji)
    }
}
