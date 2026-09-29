/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin.validation

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.lockscreen.impl.LockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.model.PinEntry

@Inject
class PinValidator(private val lockScreenConfig: LockScreenConfig) {
    sealed interface Result {
        data object Valid : Result
        data class Invalid(val failure: SetupPinFailure) : Result
    }

    fun isPinValid(pinEntry: PinEntry): Result {
        val pinAsText = pinEntry.toText()
        val isForbidden = lockScreenConfig.forbiddenPinCodes.any { it == pinAsText }
        return if (isForbidden) {
            Result.Invalid(SetupPinFailure.ForbiddenPin)
        } else {
            Result.Valid
        }
    }
}
