/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.utils

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClientProvider
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.widget.CallWidgetSettingsProvider
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import cloud.orelit.communicationplatform.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.coroutines.flow.firstOrNull

// OREL: use the bundled embedded Element Call frontend (element-call-embedded, served locally
// via WebViewAssetLoader). The RTC/SFU backend (mrtc.cp.orel.cloud) is discovered from the
// homeserver's .well-known (org.matrix.msc4143.rtc_foci at cp.orel.cloud), so no app-side SFU
// config is needed.
// NOTE: Do NOT point this at https://web.cp.orel.cloud — that host serves Element Web, not the
// Element Call web app, so the call widget never loads and calls fail with a generic error.
// To use a remote frontend instead, deploy the Element Call app (element-hq/element-call) and
// set this to that URL.
private const val EMBEDDED_CALL_WIDGET_BASE_URL = "https://appassets.androidplatform.net/element-call/index.html"

@ContributesBinding(AppScope::class)
class DefaultCallWidgetProvider(
    private val matrixClientsProvider: MatrixClientProvider,
    private val appPreferencesStore: AppPreferencesStore,
    private val callWidgetSettingsProvider: CallWidgetSettingsProvider,
    private val activeRoomsHolder: ActiveRoomsHolder,
) : CallWidgetProvider {
    override suspend fun getWidget(
        sessionId: SessionId,
        roomId: RoomId,
        isAudioCall: Boolean,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<CallWidgetProvider.GetWidgetResult> = runCatchingExceptions {
        val matrixClient = matrixClientsProvider.getOrRestore(sessionId).getOrThrow()
        val room = activeRoomsHolder.getActiveRoomMatching(sessionId, roomId)
            ?: matrixClient.getJoinedRoom(roomId)
            ?: error("Room not found")

        val customBaseUrl = appPreferencesStore.getCustomElementCallBaseUrlFlow().firstOrNull()
        val baseUrl = customBaseUrl ?: EMBEDDED_CALL_WIDGET_BASE_URL

        val roomInfo = room.info()
        val isEncrypted = roomInfo.isEncrypted ?: room.getUpdatedIsEncrypted().getOrThrow()
        val widgetSettings = callWidgetSettingsProvider.provide(
            baseUrl = baseUrl,
            encrypted = isEncrypted,
            direct = room.isDm(),
            isAudioCall = isAudioCall,
            hasActiveCall = roomInfo.hasRoomCall,
        )
        val callUrl = room.generateWidgetWebViewUrl(
            widgetSettings = widgetSettings,
            clientId = clientId,
            languageTag = languageTag,
            theme = theme,
        ).getOrThrow()

        val driver = room.getWidgetDriver(widgetSettings).getOrThrow()

        CallWidgetProvider.GetWidgetResult(
            driver = driver,
            url = callUrl,
        )
    }
}
