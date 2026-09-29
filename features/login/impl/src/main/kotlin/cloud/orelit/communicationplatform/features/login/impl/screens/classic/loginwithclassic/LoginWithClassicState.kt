/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.classic.loginwithclassic

import android.graphics.Bitmap
import androidx.compose.runtime.Stable
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

@Stable
data class LoginWithClassicState(
    val isElementPro: Boolean,
    val userId: UserId,
    val displayName: String?,
    val avatar: Bitmap?,
    val loginWithClassicAction: AsyncAction<Unit>,
    val loginModeState: LoginModeState,
    val eventSink: (LoginWithClassicEvent) -> Unit,
)
