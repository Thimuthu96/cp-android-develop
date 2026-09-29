/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.changeserver

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.MatrixHomeServerDetails
import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

data class ChangeServerState(
    // On success, carries the resolved homeserver details so the caller can proceed with login without
    // configuring (and re-networking) the homeserver a second time.
    val changeServerAction: AsyncData<MatrixHomeServerDetails>,
    val localNetworkPermissionDialog: LocalNetworkPermissionDialog,
    val eventSink: (ChangeServerEvent) -> Unit
)
