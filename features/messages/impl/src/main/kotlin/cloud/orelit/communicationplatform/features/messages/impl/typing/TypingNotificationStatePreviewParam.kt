/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.typing

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_CHARLIE
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_DAVID
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_EVE
import kotlinx.collections.immutable.toImmutableList

class TypingNotificationStatePreviewParam : PreviewParameterProvider<TypingNotificationState> {
    override val values: Sequence<TypingNotificationState>
        get() = sequenceOf(
            aTypingNotificationState(),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_ALICE),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = "Alice (@alice:example.com)"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_ALICE),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_BOB),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_ALICE),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_BOB),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_CHARLIE),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_ALICE),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_BOB),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_CHARLIE),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_DAVID),
                    aTypingRoomMember(disambiguatedDisplayName = USER_NAME_EVE),
                ),
            ),
            aTypingNotificationState(
                typingMembers = listOf(
                    aTypingRoomMember(disambiguatedDisplayName = "Alice with a very long display name which means that it will be truncated"),
                ),
            ),
            aTypingNotificationState(
                typingMembers = emptyList(),
                reserveSpace = true,
            ),
        )
}

internal fun aTypingNotificationState(
    typingMembers: List<TypingRoomMember> = emptyList(),
    reserveSpace: Boolean = false,
) = TypingNotificationState(
    renderTypingNotifications = true,
    typingMembers = typingMembers.toImmutableList(),
    reserveSpace = reserveSpace,
)

internal fun aTypingRoomMember(
    disambiguatedDisplayName: String = "@alice:example.com",
) = TypingRoomMember(
    disambiguatedDisplayName = disambiguatedDisplayName,
)
