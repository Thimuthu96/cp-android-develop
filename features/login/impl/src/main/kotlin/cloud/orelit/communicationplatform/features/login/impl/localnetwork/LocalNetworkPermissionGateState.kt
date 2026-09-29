/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.localnetwork

import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

data class LocalNetworkPermissionGateState<T>(
    val dialog: LocalNetworkPermissionDialog,
    val submit: (T) -> Unit,
    val requestPermission: () -> Unit,
    val abort: () -> Unit,
)
