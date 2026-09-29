/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.contentscanner.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.features.contentscanner.api.ContentScannerService
import cloud.orelit.communicationplatform.features.contentscanner.impl.DefaultContentScannerService
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.di.annotations.RoomCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.scanner.ContentScanner
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(RoomScope::class)
object ContentScannerBindingContainer {
    @Provides
    @SingleIn(RoomScope::class)
    fun providesContentScannerService(
        contentScanner: ContentScanner?,
        @RoomCoroutineScope coroutineScope: CoroutineScope,
        coroutineDispatchers: CoroutineDispatchers,
    ): ContentScannerService {
        return if (contentScanner != null) {
            DefaultContentScannerService(
                contentScanner = contentScanner,
                coroutineScope = coroutineScope,
                coroutineDispatchers = coroutineDispatchers,
            )
        } else {
            AlwaysValidContentScannerService()
        }
    }
}
