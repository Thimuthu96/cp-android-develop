/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.labs

import cloud.orelit.communicationplatform.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.ImmutableList

data class LabsState(
    val features: ImmutableList<FeatureUiModel>,
    val isApplyingChanges: Boolean,
    val eventSink: (LabsEvent) -> Unit,
)
