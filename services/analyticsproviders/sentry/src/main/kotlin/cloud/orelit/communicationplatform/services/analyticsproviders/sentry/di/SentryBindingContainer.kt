/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analyticsproviders.sentry.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.libraries.di.identifiers.SentryDsn
import cloud.orelit.communicationplatform.libraries.di.identifiers.SentrySdkDsn
import cloud.orelit.communicationplatform.services.analyticsproviders.sentry.SentryConfig

@BindingContainer
@ContributesTo(AppScope::class)
object SentryBindingContainer {
    @Provides
    fun providesSentryDsn(): SentryDsn? = SentryConfig.DSN.takeIf { it.isNotBlank() }?.let(::SentryDsn)

    @Provides
    fun providesSentrySdkDsn(): SentrySdkDsn? = SentryConfig.SDK_DSN.takeIf { it.isNotBlank() }?.let(::SentrySdkDsn)
}
