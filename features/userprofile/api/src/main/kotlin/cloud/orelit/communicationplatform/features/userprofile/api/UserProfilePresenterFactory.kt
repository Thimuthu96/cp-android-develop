/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.api

import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId

/**
 * Creates the user profile presenter, so that other features can show a profile without depending on this one's implementation.
 */
fun interface UserProfilePresenterFactory {
    /**
     * @param userId the user whose profile is shown.
     */
    fun create(userId: UserId): Presenter<UserProfileState>
}
