/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.changeserver

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.login.impl.error.ChangeServerError
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.MatrixHomeServerDetails
import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog

open class ChangeServerStatePreviewParam : PreviewParameterProvider<ChangeServerState> {
    override val values: Sequence<ChangeServerState>
        get() = sequenceOf(
            aChangeServerState(),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.Error(null))),
            aChangeServerState(changeServerAction = AsyncData.Failure(ChangeServerError.SlidingSyncAlert)),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.UnauthorizedAccountProvider(
                        unauthorisedAccountProviderTitle = "example.com",
                        authorisedAccountProviderTitles = listOf("element.io", "element.org"),
                    )
                )
            ),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.NeedElementPro(
                        unauthorisedAccountProviderTitle = "example.com",
                        applicationId = "applicationId",
                    ),
                )
            ),
            aChangeServerState(
                changeServerAction = AsyncData.Failure(
                    ChangeServerError.UnsupportedServer
                )
            ),
            aChangeServerState(localNetworkPermissionDialog = LocalNetworkPermissionDialog.Rationale),
        )
}

fun aChangeServerState(
    changeServerAction: AsyncData<MatrixHomeServerDetails> = AsyncData.Uninitialized,
    localNetworkPermissionDialog: LocalNetworkPermissionDialog = LocalNetworkPermissionDialog.None,
) = ChangeServerState(
    changeServerAction = changeServerAction,
    localNetworkPermissionDialog = localNetworkPermissionDialog,
    eventSink = {}
)
