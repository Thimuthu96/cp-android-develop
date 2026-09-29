/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.noop.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.libraries.di.identifiers.SentrySdkDsn

@BindingContainer
@ContributesTo(AppScope::class)
object NoopAnalyticsBindingContainer {
    @Provides
    fun providesSentrySdkDsn(): SentrySdkDsn? = null
}
