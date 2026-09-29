/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.impl

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

@Inject
class RtcNotificationContentFormatter(
    private val matrixClient: MatrixClient,
    private val sp: StringProvider,
) {
    fun format(
        content: CallNotifyContent,
        isDm: Boolean,
    ): CharSequence {
        return if (isDm) {
            val isDeclined = content.declinedBy.isNotEmpty()
            val isDeclinedByMe = content.declinedBy.any { matrixClient.isMe(it) }
            if (isDeclinedByMe) {
                sp.getString(CommonStrings.common_call_you_declined)
            } else if (isDeclined) {
                sp.getString(CommonStrings.common_call_declined)
            } else {
                sp.getString(CommonStrings.common_call_started)
            }
        } else {
            sp.getString(CommonStrings.common_call_started)
        }
    }
}
