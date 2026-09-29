/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons

enum class HomeNavigationBarItem(
    @StringRes
    val labelRes: Int,
) {
    Chats(
        labelRes = R.string.screen_home_tab_chats
    ),
    Spaces(
        labelRes = R.string.screen_home_tab_spaces
    ),
    Calendar(
        labelRes = R.string.screen_home_tab_calendar
    );

    @Composable
    fun icon(
        isSelected: Boolean,
    ) = when (this) {
        Chats -> if (isSelected) CompoundIcons.ChatSolid() else CompoundIcons.Chat()
        Spaces -> if (isSelected) CompoundIcons.SpaceSolid() else CompoundIcons.Space()
        // There is no solid variant of the calendar icon.
        Calendar -> CompoundIcons.Calendar()
    }

    companion object {
        fun from(index: Int): HomeNavigationBarItem {
            return entries.getOrElse(index) { Chats }
        }
    }
}
