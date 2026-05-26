package hr.algebra.myapplication.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Schedules / cancels the case-update poll. We use a self-rescheduling [OneTimeWorkRequest]
 * (not [PeriodicWorkRequest]) because periodic work is clamped to a 15-minute minimum interval
 * by the platform. After each run the worker enqueues the next iteration with [intervalSeconds]
 * delay — until either a change is detected (worker cancels itself) or the user flips the
 * toggle off.
 */
@Singleton
class CaseUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userManager: UserManager,
    private val authPreferences: AuthPreferences,
) {
    /**
     * Seeds the baseline snapshot from the server and enqueues the first worker run.
     * Returns true on success, false if no user is loaded or the snapshot fetch failed.
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
        scheduleNext(context, intervalSeconds)
        return true
    }

    /** Cancels the poll and clears the snapshot. Idempotent. */
    fun disable() {
        authPreferences.setCaseNotificationsEnabled(false)
        authPreferences.clearCaseSnapshot()
        WorkManager.getInstance(context).cancelUniqueWork(CaseUpdateWorker.UNIQUE_NAME)
    }

    fun isEnabled(): Boolean = authPreferences.isCaseNotificationsEnabled()

    companion object {
        /**
         * Poll interval in seconds. Production target is 600–1200 (10–20 min); set lower for
         * local verification. Periodic-work's 15-minute floor does NOT apply here — this drives
         * a chained OneTimeWorkRequest.
         */
        const val intervalSeconds: Long = 20L

        fun scheduleNext(context: Context, delaySeconds: Long) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CaseUpdateWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInputData(
                    Data.Builder()
                        .putLong(CaseUpdateWorker.KEY_INTERVAL_SECONDS, delaySeconds)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                CaseUpdateWorker.UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
            Log.d("CaseUpdateScheduler", "Enqueued next run in ${delaySeconds}s")
        }
    }
}
