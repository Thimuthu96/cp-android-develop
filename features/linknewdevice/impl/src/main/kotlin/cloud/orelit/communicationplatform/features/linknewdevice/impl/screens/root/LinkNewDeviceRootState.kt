/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.root

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData

data class LinkNewDeviceRootState(
    val isSupported: AsyncData<Boolean>,
    val qrCodeData: AsyncData<Unit>,
    val eventSink: (LinkNewDeviceRootEvent) -> Unit,
)
