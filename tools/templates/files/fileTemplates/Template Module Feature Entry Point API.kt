package cloud.orelit.communicationplatform.features.${MODULE_NAME}.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint

interface ${FEATURE_NAME}EntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        // Add your callbacks
    }
}
