/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.labs

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.icons.CompoundDrawables
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconSource
import cloud.orelit.communicationplatform.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.toImmutableList

internal class LabsStatePreviewParam : PreviewParameterProvider<LabsState> {
    override val values: Sequence<LabsState>
        get() = sequenceOf(
            aLabsState(features = aFeatureList()),
            aLabsState(features = aFeatureList(), isApplyingChanges = true),
        )
}

internal fun aLabsState(
    features: List<FeatureUiModel> = emptyList(),
    isApplyingChanges: Boolean = false,
) = LabsState(
    features = features.toImmutableList(),
    isApplyingChanges = isApplyingChanges,
    eventSink = {},
)

internal fun aFeatureList() = listOf(
    FeatureUiModel(
        key = "feature_1",
        title = "Feature 1",
        description = "This is a description of feature 1.",
        isEnabled = true,
        icon = IconSource.Resource(CompoundDrawables.ic_compound_threads),
    ),
    FeatureUiModel(
        key = "feature_2",
        title = "Feature 2",
        description = "This is a description of feature 2.",
        isEnabled = false,
        icon = IconSource.Resource(CompoundDrawables.ic_compound_video_call),
    )
)
