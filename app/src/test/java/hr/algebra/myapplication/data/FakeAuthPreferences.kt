package hr.algebra.myapplication.data

import androidx.appcompat.app.AppCompatDelegate
import hr.algebra.myapplication.models.UserProfile

/**
 * In-memory [AuthPreferences] for JVM unit tests. Mirrors the exact semantics of
 * [EncryptedAuthPreferences] (including `clearAll` preserving theme + notification keys),
 * but without touching the AndroidX keystore.
 */
class FakeAuthPreferences(
    private var token: String? = null,
    private var user: UserProfile? = null,
    private var nightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    private var caseNotifsEnabled: Boolean = false,
    private var caseSnapshot: Map<Int, String> = emptyMap(),
) : AuthPreferences {

    override fun getToken() = token
    override fun saveToken(token: String) { this.token = token }
    override fun clearToken() { this.token = null }

    override fun getUser() = user
    override fun saveUser(profile: UserProfile) { this.user = profile }
    override fun clearUser() { this.user = null }

    override fun clearAll() {
        token = null
        user = null
        // theme + notification keys preserved, matching production semantics
    }

    override fun getNightMode() = nightMode
    override fun saveNightMode(mode: Int) { this.nightMode = mode }

    override fun isCaseNotificationsEnabled() = caseNotifsEnabled
    override fun setCaseNotificationsEnabled(enabled: Boolean) { this.caseNotifsEnabled = enabled }

    override fun getCaseSnapshot() = caseSnapshot
    override fun saveCaseSnapshot(snapshot: Map<Int, String>) { this.caseSnapshot = snapshot }
    override fun clearCaseSnapshot() { this.caseSnapshot = emptyMap() }
}
