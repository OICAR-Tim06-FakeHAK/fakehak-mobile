package hr.algebra.myapplication.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

@HiltWorker
class DeployementUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepository: UserRepository,
    private val authPreferences: AuthPreferences,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Skip when the app is in the foreground — the user is already looking at the data.
        val state = ProcessLifecycleOwner.get().lifecycle.currentState
        if (state.isAtLeast(Lifecycle.State.STARTED)) {
            return@withContext Result.success()
        }

        if (!authPreferences.isCaseNotificationsEnabled()) {
            // Toggle was flipped off between schedulings; nothing to do.
            return@withContext Result.success()
        }

        val userId = authPreferences.getUser()?.id
            ?: return@withContext Result.success() // no logged-in user, bail

        val result = userRepository.getUserCases(userId)
        val cases = when (result) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> return@withContext Result.retry()
            is ApiResult.Loading -> return@withContext Result.retry()
        }

        val current = cases.associate { it.id to it.status }
        val previous = authPreferences.getCaseSnapshot()

        val changed = current.any { (id, status) -> previous[id] != status } ||
                previous.keys.any { it !in current }

        if (changed) {
            postCaseUpdateNotification()
            // Turn off polling: cancel work + flip pref + clear snapshot.
            authPreferences.setCaseNotificationsEnabled(false)
            authPreferences.clearCaseSnapshot()
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_NAME)
        }

        Result.success()
    }

    private fun postCaseUpdateNotification() {
        val ctx = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            ?: android.content.Intent(ctx, HostActivity::class.java)
        val pending = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(ctx, HrApp.CASE_UPDATES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(ctx.getString(R.string.case_notif_title))
            .setContentText(ctx.getString(R.string.case_notif_body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val UNIQUE_NAME = "case_update_worker"
        private const val NOTIFICATION_ID = 4242
    }
}