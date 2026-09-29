/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfilePresenterFactory
import cloud.orelit.communicationplatform.libraries.androidutils.clipboard.ClipboardHelper
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.EncryptionService
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom

@BindingContainer
@ContributesTo(RoomScope::class)
object RoomMemberBindingContainer {
    @Provides
    fun providesRoomMemberDetailsPresenterFactory(
        room: JoinedRoom,
        userProfilePresenterFactory: UserProfilePresenterFactory,
        encryptionService: EncryptionService,
        clipboardHelper: ClipboardHelper,
    ): RoomMemberDetailsPresenter.Factory {
        return object : RoomMemberDetailsPresenter.Factory {
            override fun create(roomMemberId: UserId): RoomMemberDetailsPresenter {
                return RoomMemberDetailsPresenter(
                    roomMemberId = roomMemberId,
                    room = room,
                    userProfilePresenterFactory = userProfilePresenterFactory,
                    encryptionService = encryptionService,
                    clipboardHelper = clipboardHelper,
                )
            }
        }
    }
}
