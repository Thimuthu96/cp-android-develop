#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun ${NAME}View(
    state: ${NAME}State,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            "${NAME} feature view",
            color = ElementTheme.colors.textPrimary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ${NAME}ViewPreview(
    @PreviewParameter(${NAME}StatePreviewParam::class) state: ${NAME}State
) = ElementPreview {
    ${NAME}View(
        state = state,
    )
}
