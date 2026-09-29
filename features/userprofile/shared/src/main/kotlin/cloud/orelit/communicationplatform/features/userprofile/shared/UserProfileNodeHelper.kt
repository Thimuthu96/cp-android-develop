/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.shared

import android.content.Context
import cloud.orelit.communicationplatform.libraries.androidutils.R
import cloud.orelit.communicationplatform.libraries.androidutils.system.startSharePlainTextIntent
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkBuilder
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import timber.log.Timber

class UserProfileNodeHelper(
    private val userId: UserId,
) {
    interface Callback : NodeInputs {
        fun navigateToAvatarPreview(username: String, avatarUrl: String)
        fun navigateToRoom(roomId: RoomId)
        fun startCall(dmRoomId: RoomId, callIntent: CallIntent)
        fun startVerifyUserFlow(userId: UserId)
    }

    fun onShareUser(
        context: Context,
        permalinkBuilder: PermalinkBuilder,
    ) {
        val permalinkResult = permalinkBuilder.permalinkForUser(userId)
        permalinkResult.onSuccess { permalink ->
            context.startSharePlainTextIntent(
                activityResultLauncher = null,
                chooserTitle = context.getString(CommonStrings.action_share),
                text = permalink,
                noActivityFoundMessage = context.getString(R.string.error_no_compatible_app_found)
            )
        }.onFailure {
            Timber.e(it)
        }
    }
}
