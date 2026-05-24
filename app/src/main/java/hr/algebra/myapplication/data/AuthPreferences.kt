package hr.algebra.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import hr.algebra.myapplication.models.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) {
    private val prefs: SharedPreferences = buildPrefs()

    private fun buildPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            // Reset on corrupted keystore (common after reinstall) and retry once.
            context.deleteSharedPreferences(FILE_NAME)
            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) = prefs.edit { putString(KEY_TOKEN, token) }

    fun clearToken() = prefs.edit { remove(KEY_TOKEN) }

    fun getUser(): UserProfile? {
        val json = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun saveUser(profile: UserProfile) =
        prefs.edit { putString(KEY_USER_JSON, gson.toJson(profile)) }

    fun clearUser() = prefs.edit { remove(KEY_USER_JSON) }

    /** Clears auth state (token + user). Preserves user-preference keys like theme. */
    fun clearAll() = prefs.edit {
        remove(KEY_TOKEN)
        remove(KEY_USER_JSON)
    }

    fun getNightMode(): Int =
        prefs.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun saveNightMode(mode: Int) = prefs.edit { putInt(KEY_NIGHT_MODE, mode) }

    companion object {
        private const val FILE_NAME = "auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_JSON = "user_profile"
        private const val KEY_NIGHT_MODE = "night_mode"
    }
}
