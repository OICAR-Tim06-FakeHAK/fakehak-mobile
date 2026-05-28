package hr.algebra.myapplication.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import hr.algebra.myapplication.HostActivity
import hr.algebra.myapplication.HrApp
import hr.algebra.myapplication.R
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@HiltWorker
class CaseUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val authPreferences: AuthPreferences,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val interval = inputData
            .getLong(
                KEY_INTERVAL_MILLIS,
                CaseUpdateScheduler.interval.inWholeMilliseconds,
            )
            .milliseconds

        Log.d(TAG, "doWork() fired; interval=$interval")

        val state = ProcessLifecycleOwner.get().lifecycle.currentState

        if (state.isAtLeast(Lifecycle.State.STARTED)) {
            Log.d(TAG, "App is foreground ($state); rescheduling without notifying.")
            rescheduleIfEnabled(interval)
            return@withContext Result.success()
        }

        if (!authPreferences.isCaseNotificationsEnabled()) {
            Log.d(TAG, "Notifications toggle off; stopping chain.")
            return@withContext Result.success()
        }

        val userId = authPreferences.getUser()?.id

        if (userId == null) {
            Log.d(TAG, "No persisted user; stopping chain.")
            return@withContext Result.success()
        }

        val cases = when (val result = userRepository.getUserCases(userId)) {
            is ApiResult.Success -> result.data

            is ApiResult.Error -> {
                Log.w(TAG, "getUserCases failed: ${result.message}; rescheduling.")
                rescheduleIfEnabled(interval)
                return@withContext Result.success()
            }

            is ApiResult.Loading -> {
                rescheduleIfEnabled(interval)
                return@withContext Result.success()
            }
        }

        val current = cases.associate { it.id to it.status }
        val previous = authPreferences.getCaseSnapshot()

        Log.d(TAG, "Snapshot: previous=$previous current=$current")

        val changed = detectCaseChanges(previous, current)

        if (changed) {
            Log.i(TAG, "Change detected — posting notification and stopping chain.")

            postCaseUpdateNotification()

            authPreferences.setCaseNotificationsEnabled(false)
            authPreferences.clearCaseSnapshot()

            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(UNIQUE_NAME)

        } else {
            Log.d(TAG, "No change; rescheduling in $interval.")
            rescheduleIfEnabled(interval)
        }

        Result.success()
    }

    private fun rescheduleIfEnabled(interval: Duration) {
        if (authPreferences.isCaseNotificationsEnabled()) {
            CaseUpdateScheduler.scheduleNext(applicationContext, interval)
        }
    }

    private fun postCaseUpdateNotification() {
        val ctx = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.w(TAG, "POST_NOTIFICATIONS not granted; skipping notify().")
                return
            }
        }

        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            ?: android.content.Intent(ctx, HostActivity::class.java)

        val pending = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            ctx,
            HrApp.CASE_UPDATES_CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(ctx.getString(R.string.case_notif_title))
            .setContentText(ctx.getString(R.string.case_notif_body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(ctx)
            .notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val UNIQUE_NAME = "case_update_worker"

        const val KEY_INTERVAL_MILLIS = "interval_millis"

        private const val NOTIFICATION_ID = 4242
        private const val TAG = "CaseUpdateWorker"
    }
}
