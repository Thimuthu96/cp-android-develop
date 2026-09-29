/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.attachments.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.compound.theme.ForcedDarkElementTheme
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.messages.impl.attachments.Attachment
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMediaRenderer
import kotlinx.collections.immutable.ImmutableList

@ContributesNode(RoomScope::class)
@AssistedInject
class AttachmentsPreviewNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: AttachmentsPreviewPresenter.Factory,
    private val localMediaRenderer: LocalMediaRenderer,
    private val sessionId: SessionId,
    private val enterpriseService: EnterpriseService,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val attachments: ImmutableList<Attachment>,
        val timelineMode: Timeline.Mode,
        val inReplyToEventId: EventId?,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    private val onDoneListener = OnDoneListener {
        navigateUp()
    }

    private val presenter = presenterFactory.create(
        attachments = inputs.attachments,
        timelineMode = inputs.timelineMode,
        onDoneListener = onDoneListener,
        inReplyToEventId = inputs.inReplyToEventId,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val colors by remember {
            enterpriseService.semanticColorsFlow(sessionId = sessionId)
        }.collectAsState(SemanticColorsLightDark.default)
        ForcedDarkElementTheme(
            colors = colors,
        ) {
            val state = presenter.present()
            AttachmentsPreviewView(
                state = state,
                localMediaRenderer = localMediaRenderer,
                modifier = modifier
            )
        }
    }
}
