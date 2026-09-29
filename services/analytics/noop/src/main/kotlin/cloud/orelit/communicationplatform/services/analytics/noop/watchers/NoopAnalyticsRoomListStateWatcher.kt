/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.noop.watchers

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsRoomListStateWatcher

@ContributesBinding(SessionScope::class)
class NoopAnalyticsRoomListStateWatcher : AnalyticsRoomListStateWatcher {
    override fun start() {}
    override fun stop() {}
}
