/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.deeplink.impl

import android.content.Intent
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.androidutils.text.urlDecoded
import cloud.orelit.communicationplatform.libraries.deeplink.api.DeeplinkData
import cloud.orelit.communicationplatform.libraries.deeplink.api.DeeplinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId

@ContributesBinding(AppScope::class)
class DefaultDeeplinkParser : DeeplinkParser {
    override fun getFromIntent(intent: Intent): DeeplinkData? {
        return intent
            .takeIf { it.action == Intent.ACTION_VIEW }
            ?.data
            ?.toDeeplinkData()
    }

    private fun Uri.toDeeplinkData(): DeeplinkData? {
        if (scheme != SCHEME) return null
        if (host != HOST) return null
        val pathBits = encodedPath.orEmpty().split("/").drop(1).map { it.urlDecoded() }
        val sessionId = pathBits.elementAtOrNull(0)?.let(::SessionId) ?: return null

        return when (val screenPathComponent = pathBits.elementAtOrNull(1)) {
            null -> DeeplinkData.Root(sessionId)
            else -> {
                val roomId = screenPathComponent.let(::RoomId)
                val threadId = pathBits.elementAtOrNull(2)?.takeIf { it.isNotBlank() }?.let(::ThreadId)
                val eventId = pathBits.elementAtOrNull(3)?.takeIf { it.isNotBlank() }?.let(::EventId)
                DeeplinkData.Room(sessionId, roomId, threadId, eventId)
            }
        }
    }
}
