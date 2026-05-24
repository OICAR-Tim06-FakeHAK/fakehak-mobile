package hr.algebra.myapplication.managers

import hr.algebra.myapplication.data.AuthPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val prefs: AuthPreferences,
) {
    fun saveToken(token: String) = prefs.saveToken(token)

    fun getToken(): String? = prefs.getToken()

    fun clear() = prefs.clearToken()

    fun isTokenValid(): Boolean {
        val token = getToken() ?: return false
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(
                payload,
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            )
            val decodedString = String(decodedBytes)
            val exp = org.json.JSONObject(decodedString).optLong("exp", 0)
            exp > System.currentTimeMillis() / 1000
        } catch (_: Exception) {
            false
        }
    }
}
