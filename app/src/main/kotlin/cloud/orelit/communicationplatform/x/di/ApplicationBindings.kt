/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.x.OrelCommunicationPlatformApplication

@ContributesTo(AppScope::class)
interface ApplicationBindings {
    fun inject(application: OrelCommunicationPlatformApplication)
}
