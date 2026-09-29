/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.blockedusers

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class BlockedUsersState(
    val blockedUsers: ImmutableList<MatrixUser>,
    val unblockUserAction: AsyncAction<Unit>,
    val snackbarMessage: SnackbarMessage?,
    val eventSink: (BlockedUsersEvent) -> Unit,
)
