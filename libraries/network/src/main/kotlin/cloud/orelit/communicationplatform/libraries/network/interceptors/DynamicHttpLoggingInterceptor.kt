/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.network.interceptors

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.libraries.matrix.api.tracing.LogLevel
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * HTTP logging interceptor that decides whether to display the HTTP logs or not based on the current log level.
 */
@Inject
@SingleIn(AppScope::class)
class DynamicHttpLoggingInterceptor(
    private val appPreferencesStore: AppPreferencesStore,
    private val loggingInterceptor: HttpLoggingInterceptor,
) : Interceptor by loggingInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // This is called in a separate thread, so calling `runBlocking` here should be fine, it should be also instant after the value is cached
        val logLevel = runBlocking { appPreferencesStore.getTracingLogLevelFlow().first() }
        loggingInterceptor.level = if (logLevel >= LogLevel.DEBUG) Level.BODY else Level.NONE
        return loggingInterceptor.intercept(chain)
    }
}
