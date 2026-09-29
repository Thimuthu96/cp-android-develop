/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.user.editprofile

import cloud.orelit.communicationplatform.libraries.matrix.ui.media.AvatarAction

sealed interface EditUserProfileEvent {
    data class HandleAvatarAction(val action: AvatarAction) : EditUserProfileEvent
    data class UpdateDisplayName(val name: String) : EditUserProfileEvent
    data object Exit : EditUserProfileEvent
    data object Save : EditUserProfileEvent
    data object CloseDialog : EditUserProfileEvent
}
