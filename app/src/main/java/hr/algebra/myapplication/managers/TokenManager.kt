package hr.algebra.myapplication.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefsName = "secure_prefs"
    private val tokenKey = "jwt_token"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(tokenKey, token)
        }
    }

    fun getToken(): String? = sharedPreferences.getString(tokenKey, null)

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
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val exp = org.json.JSONObject(decodedString).optLong("exp", 0)
            exp > System.currentTimeMillis() / 1000
        } catch (_: Exception) {
            false
        }
    }
}