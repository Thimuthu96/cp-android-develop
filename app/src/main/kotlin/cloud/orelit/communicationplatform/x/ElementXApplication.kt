/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.x

import android.app.Application
import androidx.compose.material3.ComposeMaterial3Flags.isAnchoredDraggableComponentsStrictOffsetCheckEnabled
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.startup.AppInitializer
import androidx.work.Configuration
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import cloud.orelit.communicationplatform.libraries.architecture.bindings
import cloud.orelit.communicationplatform.libraries.di.DependencyInjectionGraphOwner
import cloud.orelit.communicationplatform.libraries.matrix.api.SdkMetadata
import cloud.orelit.communicationplatform.libraries.workmanager.api.di.MetroWorkerFactory
import cloud.orelit.communicationplatform.x.di.AppGraph
import cloud.orelit.communicationplatform.x.di.ApplicationBindings
import cloud.orelit.communicationplatform.x.info.logApplicationInfo
import cloud.orelit.communicationplatform.x.initializer.CacheCleanerInitializer
import cloud.orelit.communicationplatform.x.initializer.CrashInitializer
import cloud.orelit.communicationplatform.x.initializer.PlatformInitializer

class OrelCommunicationPlatformApplication : Application(), DependencyInjectionGraphOwner, Configuration.Provider {
    override val graph: AppGraph = createGraphFactory<AppGraph.Factory>().create(this)

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setWorkerFactory(MetroWorkerFactory(graph.workerProviders))
        .build()

    @Inject lateinit var sdkMetadata: SdkMetadata

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate() {
        super.onCreate()
        AppInitializer.getInstance(this).apply {
            initializeComponent(CrashInitializer::class.java)
            initializeComponent(PlatformInitializer::class.java)
            initializeComponent(CacheCleanerInitializer::class.java)
        }

        bindings<ApplicationBindings>().inject(this)
        logApplicationInfo(this, sdkMetadata.sdkGitSha)

        // Disable the strict offset check for anchored draggable components, as it can cause issues with bottom sheets.
        // Remove once https://issuetracker.google.com/issues/477038695 is fixed.
        isAnchoredDraggableComponentsStrictOffsetCheckEnabled = false
    }
}
