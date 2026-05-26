package hr.algebra.myapplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import hr.algebra.myapplication.data.AuthPreferences
import javax.inject.Inject

@HiltAndroidApp
class HrApp : Application(), Configuration.Provider {

    @Inject lateinit var authPreferences: AuthPreferences
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(authPreferences.getNightMode())
        createCaseNotificationChannel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createCaseNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CASE_UPDATES_CHANNEL_ID,
                getString(R.string.case_updates_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.case_updates_channel_desc)
            }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CASE_UPDATES_CHANNEL_ID = "case_updates"
    }
}
