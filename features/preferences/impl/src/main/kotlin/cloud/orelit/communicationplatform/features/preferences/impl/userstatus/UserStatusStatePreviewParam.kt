/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.userstatus

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.api.user.UserStatus

internal class UserStatusStatePreviewParam : PreviewParameterProvider<UserStatusState> {
    override val values get() = sequenceOf(
        aUserStatusState(),
        aUserStatusState(displayedStatus = DisplayedStatus.UserSet(UserStatus("🌴", "Away"))),
        aUserStatusState(displayedStatus = DisplayedStatus.InCall(callJoinedTs = 0L)),
        aUserStatusState(pickerState = UserStatusPickerState.ShowingPicker),
        aUserStatusState(
            displayedStatus = DisplayedStatus.UserSet(UserStatus("🌴", "Away")),
            rawStatus = UserStatus("🌴", "Away"),
            pickerState = UserStatusPickerState.ShowingPicker,
        ),
        aUserStatusState(
            pickerState = UserStatusPickerState.CustomInput(
                emoji = "😀",
                textFieldState = TextFieldState(),
                emojiPickerSheetState = EmojiPickerSheetState.Hidden
            )
        ),
        aUserStatusState(
            pickerState = UserStatusPickerState.CustomInput(
                emoji = "🚀",
                textFieldState = TextFieldState("Working on something"),
                emojiPickerSheetState = EmojiPickerSheetState.Hidden
            )
        ),
    )
}

fun aUserStatusState(
    displayedStatus: DisplayedStatus? = null,
    rawStatus: UserStatus? = null,
    pickerState: UserStatusPickerState = UserStatusPickerState.Hidden,
    updateStatusAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (UserStatusEvent) -> Unit = {},
) = UserStatusState(
    displayedStatus = displayedStatus,
    rawStatus = rawStatus,
    pickerState = pickerState,
    updateStatusAction = updateStatusAction,
    eventSink = eventSink,
)
