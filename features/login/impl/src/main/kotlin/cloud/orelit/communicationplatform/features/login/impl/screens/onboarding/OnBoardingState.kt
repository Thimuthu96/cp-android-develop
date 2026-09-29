/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.onboarding

import androidx.annotation.DrawableRes
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData

data class OnBoardingState(
    val isAddingAccount: Boolean,
    val showBackButton: Boolean,
    val showDeveloperSettings: Boolean,
    val productionApplicationName: String,
    val defaultAccountProvider: String?,
    val mustChooseAccountProvider: Boolean,
    val canLoginWithQrCode: Boolean,
    val canCreateAccount: Boolean,
    val canReportBug: Boolean,
    val version: String,
    @DrawableRes
    val onBoardingLogoResId: Int?,
    val loginModeState: LoginModeState,
    val eventSink: (OnBoardingEvent) -> Unit,
) {
    val submitEnabled: Boolean
        get() = defaultAccountProvider != null &&
            (loginModeState.loginMode is AsyncData.Uninitialized || loginModeState.loginMode is AsyncData.Loading)
}
