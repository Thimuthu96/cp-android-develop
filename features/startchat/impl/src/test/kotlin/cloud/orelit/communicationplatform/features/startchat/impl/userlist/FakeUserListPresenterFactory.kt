/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.userlist

import cloud.orelit.communicationplatform.libraries.usersearch.api.UserRepository

class FakeUserListPresenterFactory(
    private val fakeUserListPresenter: FakeUserListPresenter = FakeUserListPresenter()
) : UserListPresenter.Factory {
    override fun create(
        args: UserListPresenterArgs,
        userRepository: UserRepository,
        userListDataStore: UserListDataStore,
    ): UserListPresenter = fakeUserListPresenter
}
