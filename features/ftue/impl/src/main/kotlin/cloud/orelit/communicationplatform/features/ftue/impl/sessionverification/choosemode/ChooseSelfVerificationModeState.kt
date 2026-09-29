/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.ftue.impl.sessionverification.choosemode

import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData

data class ChooseSelfVerificationModeState(
    val buttonsState: AsyncData<ButtonsState>,
    val directLogoutState: DirectLogoutState,
    val eventSink: (ChooseSelfVerificationModeEvent) -> Unit,
) {
    data class ButtonsState(
        val canUseAnotherDevice: Boolean,
        val canUseRecoveryKey: Boolean,
    )
}
