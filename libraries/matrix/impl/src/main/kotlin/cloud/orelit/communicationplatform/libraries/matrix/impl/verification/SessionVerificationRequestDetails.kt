/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.verification

import cloud.orelit.communicationplatform.libraries.matrix.api.core.DeviceId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.FlowId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.SessionVerificationRequestDetails
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.VerificationRequest
import cloud.orelit.communicationplatform.libraries.matrix.impl.user.map
import org.matrix.rustcomponents.sdk.SessionVerificationRequestDetails as RustSessionVerificationRequestDetails

fun RustSessionVerificationRequestDetails.map() = SessionVerificationRequestDetails(
    senderProfile = senderProfile.map(),
    flowId = FlowId(flowId),
    deviceId = DeviceId(deviceId),
    deviceDisplayName = deviceDisplayName,
    firstSeenTimestamp = firstSeenTimestamp.toLong(),
)

fun RustSessionVerificationRequestDetails.toVerificationRequest(currentUserId: UserId): VerificationRequest.Incoming {
    val details = map()
    return if (currentUserId == details.senderProfile.userId) {
        VerificationRequest.Incoming.OtherSession(details)
    } else {
        VerificationRequest.Incoming.User(details)
    }
}
