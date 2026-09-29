/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import cloud.orelit.communicationplatform.features.api.MigrationEntryPoint
import cloud.orelit.communicationplatform.features.enterprise.api.AppStartupHook
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.lockscreen.api.LockScreenEntryPoint
import cloud.orelit.communicationplatform.features.lockscreen.api.LockScreenService
import cloud.orelit.communicationplatform.features.rageshake.api.reporter.BugReporter
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.di.identifiers.SentrySdkDsn
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.matrix.api.platform.InitPlatformService
import cloud.orelit.communicationplatform.libraries.matrix.api.tracing.TracingService
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService

@ContributesTo(AppScope::class)
interface AppBindings {
    fun snackbarDispatcher(): SnackbarDispatcher

    fun tracingService(): TracingService

    fun platformService(): InitPlatformService

    fun bugReporter(): BugReporter

    fun lockScreenService(): LockScreenService

    fun preferencesStore(): AppPreferencesStore

    fun migrationEntryPoint(): MigrationEntryPoint

    fun lockScreenEntryPoint(): LockScreenEntryPoint

    fun analyticsService(): AnalyticsService

    fun enterpriseService(): EnterpriseService

    fun featureFlagService(): FeatureFlagService

    fun buildMeta(): BuildMeta

    fun sentrySdkDsn(): SentrySdkDsn?

    @Multibinds(allowEmpty = true)
    fun appStartupHooks(): Set<AppStartupHook>
}
