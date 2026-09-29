/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.di

import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.login.impl.qrcode.QrCodeLoginManager

@ContributesTo(QrCodeLoginScope::class)
interface QrCodeLoginBindings {
    fun qrCodeLoginManager(): QrCodeLoginManager
}
