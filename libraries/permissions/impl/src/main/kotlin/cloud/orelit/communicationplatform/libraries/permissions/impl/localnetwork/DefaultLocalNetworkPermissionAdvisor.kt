/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.permissions.impl.localnetwork

import android.Manifest
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.androidutils.network.LocalNetworkAddressClassifier
import cloud.orelit.communicationplatform.libraries.androidutils.network.LocalNetworkClassification
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionStateProvider
import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionAdvisor
import cloud.orelit.communicationplatform.services.toolbox.api.sdk.BuildVersionSdkIntProvider

@ContributesBinding(AppScope::class)
class DefaultLocalNetworkPermissionAdvisor(
    private val classifier: LocalNetworkAddressClassifier,
    private val permissionStateProvider: PermissionStateProvider,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : LocalNetworkPermissionAdvisor {
    override suspend fun shouldRequestPermissionFor(homeserverUrl: String): Boolean {
        if (!buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.CINNAMON_BUN)) return false
        if (permissionStateProvider.isPermissionGranted(Manifest.permission.ACCESS_LOCAL_NETWORK)) return false
        return when (classifier.classify(homeserverUrl)) {
            LocalNetworkClassification.LocalIp -> true
            LocalNetworkClassification.PublicIp,
            // Unresolvable: we can't confirm the host is local, so we don't prompt, acceptable trade-off vs. over-prompting.
            LocalNetworkClassification.Unresolvable -> false
        }
    }
}
