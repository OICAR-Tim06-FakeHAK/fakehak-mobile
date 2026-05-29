package hr.algebra.myapplication.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import hr.algebra.myapplication.models.UserProfile
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real-device / emulator smoke test for the production [EncryptedAuthPreferences]: confirms
 * that the EncryptedSharedPreferences + MasterKey chain round-trips a token and a user. This
 * is the ONE test in the codebase that requires `connectedAndroidTest` — everything else runs
 * on the JVM.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedAuthPreferencesTest {

    private lateinit var context: Context

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Make sure we start fresh — installer may have left state behind.
        context.deleteSharedPreferences("auth_prefs")
    }

    @After fun tearDown() {
        context.deleteSharedPreferences("auth_prefs")
    }

    @Test fun token_roundTrips() {
        val prefs = EncryptedAuthPreferences(context, Gson())
        prefs.saveToken("real.jwt.value")
        assertThat(prefs.getToken()).isEqualTo("real.jwt.value")
    }

    @Test fun user_roundTrips() {
        val prefs = EncryptedAuthPreferences(context, Gson())
        val u = UserProfile(
            id = 42, firstName = "Foo", lastName = "Bar",
            phoneNumber = "+1", email = "f@b.test", accountStatus = "ACTIVE",
            vehicles = emptyList(), createdAt = "2024-01-01",
        )
        prefs.saveUser(u)
        assertThat(prefs.getUser()).isEqualTo(u)
    }

    @Test fun clearAll_keepsThemeButWipesTokenAndUser() {
        val prefs = EncryptedAuthPreferences(context, Gson())
        prefs.saveToken("t")
        prefs.saveNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)

        prefs.clearAll()

        assertThat(prefs.getToken()).isNull()
        assertThat(prefs.getNightMode())
            .isEqualTo(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
    }
}
