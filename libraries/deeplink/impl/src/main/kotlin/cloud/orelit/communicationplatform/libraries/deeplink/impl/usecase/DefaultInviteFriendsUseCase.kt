/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.deeplink.impl.usecase

import android.app.Activity
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.androidutils.system.startSharePlainTextIntent
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.deeplink.api.usecase.InviteFriendsUseCase
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkBuilder
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import cloud.orelit.communicationplatform.libraries.androidutils.R as AndroidUtilsR

@ContributesBinding(SessionScope::class)
class DefaultInviteFriendsUseCase(
    private val stringProvider: StringProvider,
    private val matrixClient: MatrixClient,
    private val buildMeta: BuildMeta,
    private val permalinkBuilder: PermalinkBuilder,
) : InviteFriendsUseCase {
    override fun execute(activity: Activity) {
        val permalinkResult = permalinkBuilder.permalinkForUser(matrixClient.sessionId)
        permalinkResult.fold(
            onSuccess = { permalink ->
                val appName = buildMeta.applicationName
                activity.startSharePlainTextIntent(
                    activityResultLauncher = null,
                    chooserTitle = stringProvider.getString(CommonStrings.action_invite_friends),
                    text = stringProvider.getString(CommonStrings.invite_friends_text, appName, permalink),
                    extraTitle = stringProvider.getString(CommonStrings.invite_friends_rich_title, appName),
                    noActivityFoundMessage = stringProvider.getString(AndroidUtilsR.string.error_no_compatible_app_found)
                )
            },
            onFailure = {
                Timber.e(it)
            }
        )
    }
}
