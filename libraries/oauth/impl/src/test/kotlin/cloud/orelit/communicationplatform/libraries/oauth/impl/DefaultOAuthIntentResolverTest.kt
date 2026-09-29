/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.oauth.impl

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import cloud.orelit.communicationplatform.libraries.oauth.api.OAuthAction
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class DefaultOAuthIntentResolverTest : RobolectricTest() {
    @Test
    fun `test resolve OAuth go back`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "cloud.orelit.communicationplatform:/?error=access_denied&state=IFF1UETGye2ZA8pO".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(OAuthAction.GoBack())
    }

    @Test
    fun `test resolve OAuth success`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "cloud.orelit.communicationplatform:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isEqualTo(
            OAuthAction.Success(
                url = "cloud.orelit.communicationplatform:/?state=IFF1UETGye2ZA8pO&code=y6X1GZeqA3xxOWcTeShgv8nkgFJXyzWB"
            )
        )
    }

    @Test
    fun `test resolve OAuth invalid`() {
        val sut = createDefaultOAuthIntentResolver()
        val intent = Intent(RuntimeEnvironment.getApplication(), Activity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = "cloud.orelit.communicationplatform:/invalid".toUri()
        }
        val result = sut.resolve(intent)
        assertThat(result).isNull()
    }

    private fun createDefaultOAuthIntentResolver(): DefaultOAuthIntentResolver {
        return DefaultOAuthIntentResolver(
            oAuthUrlParser = DefaultOAuthUrlParser(
                oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
            ),
        )
    }
}
