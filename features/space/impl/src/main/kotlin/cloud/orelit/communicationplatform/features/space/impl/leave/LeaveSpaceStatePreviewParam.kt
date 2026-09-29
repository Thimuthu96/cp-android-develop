/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.leave

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.SPACE_NAME
import cloud.orelit.communicationplatform.libraries.matrix.api.room.join.JoinRule
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom
import cloud.orelit.communicationplatform.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class LeaveSpaceStatePreviewParam : PreviewParameterProvider<LeaveSpaceState> {
    override val values: Sequence<LeaveSpaceState>
        get() = sequenceOf(
            aLeaveSpaceState(),
            aLeaveSpaceState(
                spaceName = null,
                selectableSpaceRooms = AsyncData.Success(persistentListOf()),
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    persistentListOf(
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                displayName = "A long space name that should be truncated",
                                worldReadable = true,
                            ),
                            isLastOwner = true,
                        ),
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                joinRule = JoinRule.Invite,
                            ),
                            isSelected = false,
                        ),
                    )
                )
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    persistentListOf(
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                worldReadable = true,
                            ),
                            isLastOwner = true,
                        ),
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                joinRule = JoinRule.Invite,
                            ),
                            isSelected = true,
                        ),
                    )
                )
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    persistentListOf(
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                worldReadable = true,
                            ),
                            isLastOwner = true,
                        ),
                    )
                ),
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    persistentListOf(
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(
                                worldReadable = true,
                            ),
                            isLastOwner = true,
                        ),
                        aSelectableSpaceRoom(
                            spaceRoom = aSpaceRoom(),
                            isLastOwner = true,
                        ),
                    )
                ),
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    List(10) { aSelectableSpaceRoom() }.toImmutableList()
                ),
                leaveSpaceAction = AsyncAction.Loading,
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Success(
                    List(10) { aSelectableSpaceRoom() }.toImmutableList()
                ),
                leaveSpaceAction = AsyncAction.Failure(Exception("An error")),
            ),
            aLeaveSpaceState(
                selectableSpaceRooms = AsyncData.Failure(Exception("An error")),
            ),
            aLeaveSpaceState(
                isLastOwner = true,
            ),
            aLeaveSpaceState(
                isLastOwner = true,
                areCreatorsPrivileged = true,
            ),
        )
}

fun aLeaveSpaceState(
    spaceName: String? = SPACE_NAME,
    isLastOwner: Boolean = false,
    areCreatorsPrivileged: Boolean = false,
    selectableSpaceRooms: AsyncData<ImmutableList<SelectableSpaceRoom>> = AsyncData.Uninitialized,
    leaveSpaceAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
) = LeaveSpaceState(
    spaceName = spaceName,
    needsOwnerChange = isLastOwner,
    areCreatorsPrivileged = areCreatorsPrivileged,
    selectableSpaceRooms = selectableSpaceRooms,
    leaveSpaceAction = leaveSpaceAction,
    eventSink = { }
)

fun aSelectableSpaceRoom(
    spaceRoom: SpaceRoom = aSpaceRoom(),
    isLastOwner: Boolean = false,
    joinedMembersCount: Int = 2,
    isSelected: Boolean = false,
) = SelectableSpaceRoom(
    spaceRoom = spaceRoom,
    isLastOwner = isLastOwner,
    joinedMembersCount = joinedMembersCount,
    isSelected = isSelected,
)
