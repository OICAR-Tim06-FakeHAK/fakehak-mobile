package hr.algebra.myapplication.data

import hr.algebra.myapplication.models.UserProfile

/**
 * Persistence contract for auth + user-preference state. The production implementation is
 * [EncryptedAuthPreferences] (backed by `EncryptedSharedPreferences`). Tests substitute an
 * in-memory fake so they can run on the JVM without the AndroidX keystore.
 */
interface AuthPreferences {
    // ─── Token ────────────────────────────────────────────────────────────────
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()

    // ─── User profile ─────────────────────────────────────────────────────────
    fun getUser(): UserProfile?
    fun saveUser(profile: UserProfile)
    fun clearUser()

    /** Clears auth state (token + user). Preserves theme + notification-toggle keys. */
    fun clearAll()

    // ─── Theme ────────────────────────────────────────────────────────────────
    fun getNightMode(): Int
    fun saveNightMode(mode: Int)

    // ─── Case-update notifications ────────────────────────────────────────────
    fun isCaseNotificationsEnabled(): Boolean
    fun setCaseNotificationsEnabled(enabled: Boolean)

    fun getCaseSnapshot(): Map<Int, String>
    fun saveCaseSnapshot(snapshot: Map<Int, String>)
    fun clearCaseSnapshot()
}
