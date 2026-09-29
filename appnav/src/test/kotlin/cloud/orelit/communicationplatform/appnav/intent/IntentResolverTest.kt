/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.intent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.login.api.LoginParams
import cloud.orelit.communicationplatform.features.login.test.FakeLoginIntentResolver
import cloud.orelit.communicationplatform.features.share.api.ShareIntentData
import cloud.orelit.communicationplatform.features.share.api.UriToShare
import cloud.orelit.communicationplatform.features.share.test.FakeShareIntentHandler
import cloud.orelit.communicationplatform.libraries.deeplink.api.DeeplinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_THREAD_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkParser
import cloud.orelit.communicationplatform.libraries.oauth.api.OAuthAction
import cloud.orelit.communicationplatform.libraries.oauth.test.FakeOAuthIntentResolver
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class IntentResolverTest : RobolectricTest() {
    @Test
    fun `resolve launcher intent should return null`() {
        val sut = createIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `resolve intent replayed from the history should return null`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
                eventId = null,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve navigation intent root`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Root(A_SESSION_ID)
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Root(
                    sessionId = A_SESSION_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent room`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
                eventId = null,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = null,
                    eventId = null,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent thread`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
                eventId = null,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = A_THREAD_ID,
                    eventId = null,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent event`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = null,
                eventId = AN_EVENT_ID,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = null,
                    eventId = AN_EVENT_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve navigation intent thread and event`() {
        val sut = createIntentResolver(
            deeplinkParserResult = DeeplinkData.Room(
                sessionId = A_SESSION_ID,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
                eventId = AN_EVENT_ID,
            )
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Navigation(
                deeplinkData = DeeplinkData.Room(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    threadId = A_THREAD_ID,
                    eventId = AN_EVENT_ID,
                )
            )
        )
    }

    @Test
    fun `test resolve OAuth`() {
        val sut = createIntentResolver(
            oAuthIntentResolverResult = { OAuthAction.GoBack() },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "cloud.orelit.communicationplatform:/?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.OAuth(
                oAuthAction = OAuthAction.GoBack()
            )
        )
    }

    @Test
    fun `test resolve external permalink`() {
        val permalinkData = PermalinkData.UserLink(
            userId = UserId("@alice:matrix.org")
        )
        val sut = createIntentResolver(
            loginIntentResolverResult = { null },
            permalinkParserResult = { permalinkData },
            oAuthIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "https://matrix.to/#/@alice:matrix.org".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            ResolvedIntent.Permalink(
                permalinkData = permalinkData
            )
        )
    }

    @Test
    fun `test resolve external permalink, FallbackLink should be ignored`() {
        val sut = createIntentResolver(
            permalinkParserResult = { PermalinkData.FallbackLink(Uri.parse("https://matrix.org")) },
            loginIntentResolverResult = { null },
            oAuthIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "https://matrix.to/#/@alice:matrix.org".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve external permalink, invalid action`() {
        val permalinkData = PermalinkData.UserLink(
            userId = UserId("@alice:matrix.org")
        )
        val sut = createIntentResolver(
            permalinkParserResult = { permalinkData },
            oAuthIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_BATTERY_LOW
            data = "https://matrix.to/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test incoming share simple`() {
        val shareIntentData = ShareIntentData.PlainText("Hello")
        val sut = createIntentResolver(
            oAuthIntentResolverResult = { null },
            onIncomingShareIntent = { shareIntentData },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Hello")
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.IncomingShare(shareIntentData))
    }

    @Test
    fun `test incoming share multiple`() {
        val fileUri = "content://com.example.app/file1.jpg".toUri()
        val shareIntentData = ShareIntentData.Uris(text = "Hello", uris = listOf(UriToShare(fileUri, "image/jpg")))
        val sut = createIntentResolver(
            oAuthIntentResolverResult = { null },
            onIncomingShareIntent = { shareIntentData },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putExtra(Intent.EXTRA_TEXT, "Hello")
            data = fileUri
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.IncomingShare(shareIntentData))
    }

    @Test
    fun `test resolve invalid`() {
        val sut = createIntentResolver(
            permalinkParserResult = { PermalinkData.FallbackLink(Uri.parse("https://matrix.org")) },
            loginIntentResolverResult = { null },
            oAuthIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "io.element:/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    @Test
    fun `test resolve login param`() {
        val aLoginParams = LoginParams("accountProvider", null)
        val sut = createIntentResolver(
            loginIntentResolverResult = { aLoginParams },
            oAuthIntentResolverResult = { null },
        )
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(ResolvedIntent.Login(aLoginParams))
    }

    private fun createIntentResolver(
        deeplinkParserResult: DeeplinkData? = null,
        permalinkParserResult: (String) -> PermalinkData = { lambdaError() },
        loginIntentResolverResult: (String) -> LoginParams? = { lambdaError() },
        oAuthIntentResolverResult: (Intent) -> OAuthAction? = { lambdaError() },
        onIncomingShareIntent: (Intent) -> ShareIntentData? = { null },
    ): IntentResolver {
        return IntentResolver(
            deeplinkParser = { deeplinkParserResult },
            loginIntentResolver = FakeLoginIntentResolver(
                parseResult = loginIntentResolverResult,
            ),
            oAuthIntentResolver = FakeOAuthIntentResolver(
                resolveResult = oAuthIntentResolverResult,
            ),
            permalinkParser = FakePermalinkParser(
                result = permalinkParserResult
            ),
            shareIntentHandler = FakeShareIntentHandler(
                onIncomingShareIntent = onIncomingShareIntent,
            ),
        )
    }
}
