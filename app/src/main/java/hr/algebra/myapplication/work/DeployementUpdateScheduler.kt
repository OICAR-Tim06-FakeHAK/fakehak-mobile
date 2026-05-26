package hr.algebra.myapplication.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeployementUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userManager: UserManager,
    private val authPreferences: AuthPreferences,
) {

    /**
     * Seeds the baseline snapshot from the server and enqueues the periodic worker. Call this
     * when the user flips the toggle on. Returns true on success, false if no user is loaded or
     * the snapshot fetch failed.
     */
    suspend fun enable(): Boolean {
        when (val result = userManager.getCases()) {
            is ApiResult.Success -> {
                val snapshot = result.data.associate { it.id to it.status }
                authPreferences.saveCaseSnapshot(snapshot)
            }
            is ApiResult.Error -> return false
            is ApiResult.Loading -> return false
        }

        authPreferences.setCaseNotificationsEnabled(true)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<DeployementUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DeployementUpdateWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        return true
    }

    /** Cancels the periodic worker and clears the snapshot. Idempotent. */
    fun disable() {
        authPreferences.setCaseNotificationsEnabled(false)
        authPreferences.clearCaseSnapshot()
        WorkManager.getInstance(context).cancelUniqueWork(DeployementUpdateWorker.UNIQUE_NAME)
    }

    fun isEnabled(): Boolean = authPreferences.isCaseNotificationsEnabled()
}