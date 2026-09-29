/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.sync

import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SlidingSyncVersion
import org.matrix.rustcomponents.sdk.SlidingSyncVersion as RustSlidingSyncVersion

internal fun RustSlidingSyncVersion.map(): SlidingSyncVersion {
    return when (this) {
        RustSlidingSyncVersion.NONE -> SlidingSyncVersion.None
        RustSlidingSyncVersion.NATIVE -> SlidingSyncVersion.Native
    }
}
