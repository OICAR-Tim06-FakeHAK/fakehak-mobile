package hr.algebra.myapplication.managers

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import org.json.JSONObject

class TokenManager(context: Context) {
    private val prefsName = "secure_prefs"
    private val tokenKey = "jwt_token"

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        prefsName,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(tokenKey, token)
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(tokenKey, null)
    }

    fun clear() {
        sharedPreferences.edit {
            remove(tokenKey)
        }
    }

    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes)

            val json = JSONObject(decodedString)
            val exp = json.optLong("exp", 0)

            val currentTime = System.currentTimeMillis() / 1000

            exp > currentTime
        } catch (e: Exception) {
            false
        }
    }
}