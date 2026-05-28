package hr.algebra.myapplication.managers

import android.app.Application
import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.data.FakeAuthPreferences
import java.util.Base64
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric is needed for `android.util.Base64` used inside [TokenManager.isTokenValid].
 * We override the Application to a stock [Application] so Robolectric does NOT spin up the
 * production [hr.algebra.myapplication.HrApp] (which would build the Hilt graph including
 * `EncryptedAuthPreferences` → AndroidKeyStore not available on JVM).
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [33])
class TokenManagerTest {

    private lateinit var prefs: FakeAuthPreferences
    private lateinit var tokenManager: TokenManager

    @Before fun setUp() {
        prefs = FakeAuthPreferences()
        tokenManager = TokenManager(prefs)
    }

    @Test fun `save then get round-trips`() {
        tokenManager.saveToken("abc")
        assertThat(tokenManager.getToken()).isEqualTo("abc")
    }

    @Test fun `clear wipes the token only`() {
        tokenManager.saveToken("abc")
        tokenManager.clear()
        assertThat(tokenManager.getToken()).isNull()
    }

    @Test fun `isTokenValid is false when no token`() {
        assertThat(tokenManager.isTokenValid()).isFalse()
    }

    @Test fun `isTokenValid is true for unexpired token`() {
        tokenManager.nowMillis = { 1_000_000_000_000L } // year ~2001
        tokenManager.saveToken(jwt(expSeconds = 2_000_000_000L)) // far future
        assertThat(tokenManager.isTokenValid()).isTrue()
    }

    @Test fun `isTokenValid is false for expired token`() {
        tokenManager.nowMillis = { 2_000_000_000_000L } // year ~2033
        tokenManager.saveToken(jwt(expSeconds = 1_000L))
        assertThat(tokenManager.isTokenValid()).isFalse()
    }

    @Test fun `isTokenValid is false for malformed token`() {
        tokenManager.saveToken("not.a.token.with.too.many.dots")
        assertThat(tokenManager.isTokenValid()).isFalse()

        tokenManager.saveToken("only-one-part")
        assertThat(tokenManager.isTokenValid()).isFalse()
    }

    @Test fun `isTokenValid is false when exp claim missing`() {
        tokenManager.saveToken(jwtRaw("""{"sub":"foo"}"""))
        assertThat(tokenManager.isTokenValid()).isFalse()
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private fun jwt(expSeconds: Long): String = jwtRaw("""{"exp":$expSeconds}""")

    private fun jwtRaw(payloadJson: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
        val payload = encoder.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.sig"
    }
}
