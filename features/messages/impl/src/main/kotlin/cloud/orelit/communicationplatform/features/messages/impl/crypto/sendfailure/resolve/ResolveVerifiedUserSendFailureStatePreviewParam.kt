/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE

open class ResolveVerifiedUserSendFailureStatePreviewParam : PreviewParameterProvider<ResolveVerifiedUserSendFailureState> {
    override val values: Sequence<ResolveVerifiedUserSendFailureState>
        get() = sequenceOf(
            aResolveVerifiedUserSendFailureState(),
            aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = anUnsignedDeviceSendFailure()
            ),
            aResolveVerifiedUserSendFailureState(
                verifiedUserSendFailure = aChangedIdentitySendFailure()
            )
        )
}

fun aResolveVerifiedUserSendFailureState(
    verifiedUserSendFailure: VerifiedUserSendFailure = VerifiedUserSendFailure.None,
    resolveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    retryAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ResolveVerifiedUserSendFailureEvent) -> Unit = {}
) = ResolveVerifiedUserSendFailureState(
    verifiedUserSendFailure = verifiedUserSendFailure,
    resolveAction = resolveAction,
    retryAction = retryAction,
    eventSink = eventSink
)

fun anUnsignedDeviceSendFailure(userDisplayName: String = USER_NAME_ALICE) = VerifiedUserSendFailure.UnsignedDevice.FromOther(
    userDisplayName = userDisplayName,
)

fun aChangedIdentitySendFailure(userDisplayName: String = USER_NAME_ALICE) = VerifiedUserSendFailure.ChangedIdentity(
    userDisplayName = userDisplayName,
)
