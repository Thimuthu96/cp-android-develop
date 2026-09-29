/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.impl

import android.content.Context
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.CallNotifyContent
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID_3
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.services.toolbox.impl.strings.AndroidStringProvider
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("LargeClass")
class RtcNotificationContentFormatterTest : RobolectricTest() {
    private lateinit var context: Context
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var formatter: RtcNotificationContentFormatter

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() as Context
        fakeMatrixClient = FakeMatrixClient()
        val stringProvider = AndroidStringProvider(context.resources)
        formatter = RtcNotificationContentFormatter(
            fakeMatrixClient,
            stringProvider
        )
    }

    @Test
    @Config(qualifiers = "en")
    fun `Should not display declined info in rooms`() {
        val result = formatter.format(
            CallNotifyContent(
                callIntent = CallIntent.VIDEO,
                declinedBy = listOf(A_USER_ID_2, A_USER_ID_3),
                activeMembers = emptyList(),
                isJoined = false,
                callStartTsMillis = 0
            ),
            false
        )
        val expected = "Call started"
        assertThat(result.toString()).isEqualTo(expected)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Declined by me variant`() {
        val result = formatter.format(
            CallNotifyContent(
                CallIntent.VIDEO,
                declinedBy = listOf(fakeMatrixClient.sessionId),
                activeMembers = listOf(A_USER_ID_2),
                isJoined = false,
                callStartTsMillis = 0
            ),
            true
        )
        val expected = "You declined a call"
        assertThat(result.toString()).isEqualTo(expected)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Declined by other variant`() {
        val result = formatter.format(
            CallNotifyContent(
                CallIntent.VIDEO,
                declinedBy = listOf(A_USER_ID_2),
                activeMembers = listOf(fakeMatrixClient.sessionId),
                isJoined = true,
                callStartTsMillis = 0
            ),
            true
        )
        val expected = "Call declined"
        assertThat(result.toString()).isEqualTo(expected)
    }

    @Test
    @Config(qualifiers = "en")
    fun `Call started in DM`() {
        val result = formatter.format(
            CallNotifyContent(
                CallIntent.AUDIO,
                declinedBy = listOf(),
                activeMembers = emptyList(),
                isJoined = false,
                callStartTsMillis = 0
            ),
            true
        )
        val expected = "Call started"
        assertThat(result.toString()).isEqualTo(expected)
    }
}
