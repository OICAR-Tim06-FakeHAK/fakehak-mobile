package hr.algebra.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import hr.algebra.myapplication.models.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedAuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) : AuthPreferences {

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

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    override fun saveToken(token: String) = prefs.edit { putString(KEY_TOKEN, token) }
    override fun clearToken() = prefs.edit { remove(KEY_TOKEN) }

    override fun getUser(): UserProfile? {
        val json = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    override fun saveUser(profile: UserProfile) =
        prefs.edit { putString(KEY_USER_JSON, gson.toJson(profile)) }

    override fun clearUser() = prefs.edit { remove(KEY_USER_JSON) }

    override fun clearAll() = prefs.edit {
        remove(KEY_TOKEN)
        remove(KEY_USER_JSON)
    }

    override fun getNightMode(): Int =
        prefs.getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    override fun saveNightMode(mode: Int) = prefs.edit { putInt(KEY_NIGHT_MODE, mode) }

    override fun isCaseNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_CASE_NOTIFS_ENABLED, false)

    override fun setCaseNotificationsEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_CASE_NOTIFS_ENABLED, enabled) }

    override fun getCaseSnapshot(): Map<Int, String> {
        val json = prefs.getString(KEY_CASE_SNAPSHOT, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<Int, String>>() {}.type
            gson.fromJson<Map<Int, String>>(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    override fun saveCaseSnapshot(snapshot: Map<Int, String>) =
        prefs.edit { putString(KEY_CASE_SNAPSHOT, gson.toJson(snapshot)) }

    override fun clearCaseSnapshot() = prefs.edit { remove(KEY_CASE_SNAPSHOT) }

    companion object {
        private const val FILE_NAME = "auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_JSON = "user_profile"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val KEY_CASE_NOTIFS_ENABLED = "case_notifs_enabled"
        private const val KEY_CASE_SNAPSHOT = "case_snapshot"
    }
}
