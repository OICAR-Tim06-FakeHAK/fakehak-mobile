package hr.algebra.myapplication.repository

import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.data.FakeAuthPreferences
import hr.algebra.myapplication.managers.TokenManager
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.LoginResponse
import hr.algebra.myapplication.models.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import android.app.Application
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [33])
class AuthRepositoryTest {

    private val userRepo = mockk<UserRepository>()
    private val userManager = mockk<UserManager>(relaxed = true)

    @Test fun `login success saves token then loads profile`() = runTest {
        val prefs = FakeAuthPreferences()
        val tokenManager = TokenManager(prefs)
        val auth = AuthRepository(userRepo, tokenManager, userManager, prefs)

        coEvery { userRepo.login(any()) } returns ApiResult.Success(LoginResponse("tok.en.sig", "USER"))
        coEvery { userManager.load() } returns ApiResult.Success(sampleUser())

        val result = auth.login(LoginRequest("u@test", "pw"))

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat(prefs.getToken()).isEqualTo("tok.en.sig")
        coVerify(exactly = 1) { userManager.load() }
    }

    @Test fun `login rolls back token when profile load fails`() = runTest {
        val prefs = FakeAuthPreferences()
        val tokenManager = TokenManager(prefs)
        val auth = AuthRepository(userRepo, tokenManager, userManager, prefs)

        coEvery { userRepo.login(any()) } returns ApiResult.Success(LoginResponse("tok.en.sig", "USER"))
        coEvery { userManager.load() } returns ApiResult.Error(message = "boom")

        val result = auth.login(LoginRequest("u@test", "pw"))

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        assertThat(prefs.getToken()).isNull()  // rolled back
    }

    @Test fun `login failure does not save token`() = runTest {
        val prefs = FakeAuthPreferences()
        val tokenManager = TokenManager(prefs)
        val auth = AuthRepository(userRepo, tokenManager, userManager, prefs)

        coEvery { userRepo.login(any()) } returns ApiResult.Error(message = "401")

        val result = auth.login(LoginRequest("u@test", "pw"))

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        assertThat(prefs.getToken()).isNull()
        coVerify(exactly = 0) { userManager.load() }
    }

    @Test fun `hasPersistedSession requires both token and user`() {
        val prefs = FakeAuthPreferences()
        val tokenManager = TokenManager(prefs).apply { nowMillis = { 0L } }
        val auth = AuthRepository(userRepo, tokenManager, userManager, prefs)

        // No token, no user → false
        assertThat(auth.hasPersistedSession()).isFalse()

        // Token-only, no user → false
        prefs.saveToken(farFutureJwt())
        assertThat(auth.hasPersistedSession()).isFalse()

        // Token + user → true
        prefs.saveUser(sampleUser())
        assertThat(auth.hasPersistedSession()).isTrue()
    }

    @Test fun `logout delegates to UserManager`() = runTest {
        val prefs = FakeAuthPreferences()
        val tokenManager = TokenManager(prefs)
        val auth = AuthRepository(userRepo, tokenManager, userManager, prefs)

        auth.logout()

        coVerify(exactly = 1) { userManager.clear() }
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private fun sampleUser() = UserProfile(
        id = 7, firstName = "F", lastName = "L", phoneNumber = "+1",
        email = "e@e.test", accountStatus = "ACTIVE", vehicles = emptyList(),
        createdAt = "2024-01-01",
    )

    /** JWT whose `exp` is far in the future, so it parses as valid given a nowMillis of 0. */
    private fun farFutureJwt(): String {
        val encoder = java.util.Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
        val payload = encoder.encodeToString("""{"exp":9999999999}""".toByteArray())
        return "$header.$payload.sig"
    }
}
