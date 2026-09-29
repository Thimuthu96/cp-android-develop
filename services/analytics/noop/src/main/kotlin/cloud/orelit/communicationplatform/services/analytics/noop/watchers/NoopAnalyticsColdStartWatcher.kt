/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.noop.watchers

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsColdStartWatcher

@ContributesBinding(AppScope::class)
class NoopAnalyticsColdStartWatcher : AnalyticsColdStartWatcher {
    override fun start() {}
    override fun whenLoggingIn() {}
    override fun onRoomListVisible() {}
}
