/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.login

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

fun aLoginModeState(
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    localNetworkPermissionDialog: LocalNetworkPermissionDialog = LocalNetworkPermissionDialog.None,
    eventSink: (LoginModeEvent) -> Unit = {},
) = LoginModeState(
    loginMode = loginMode,
    localNetworkPermissionDialog = localNetworkPermissionDialog,
    eventSink = eventSink,
)
