/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.noop.watchers

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsSendMessageWatcher

@ContributesBinding(RoomScope::class)
class NoopAnalyticsSendMessageWatcher : AnalyticsSendMessageWatcher {
    override fun start() = Unit
    override fun stop() = Unit
}
