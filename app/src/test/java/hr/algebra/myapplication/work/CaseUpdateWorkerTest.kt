package hr.algebra.myapplication.work

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.HrApp
import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.data.FakeAuthPreferences
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.CaseProfile
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Drives [CaseUpdateWorker.doWork] directly via WorkManager's test API. Application is forced
 * to stock [Application] so Hilt's full graph (including [data.EncryptedAuthPreferences] which
 * requires AndroidKeyStore) is NOT constructed. The worker receives its deps from a hand-built
 * [WorkerFactory] instead.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [33])
class CaseUpdateWorkerTest {

    private lateinit var context: Context
    private lateinit var repo: UserRepository
    private lateinit var prefs: FakeAuthPreferences

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repo = mockk(relaxed = true)
        prefs = FakeAuthPreferences()

        // Runtime POST_NOTIFICATIONS gate (API 33+) is off by default in Robolectric — grant it
        // so the notification-posting branch is exercised end-to-end.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shadowOf(context as Application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            check(
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED
            )
        }

        // Required because the worker calls WorkManager.getInstance(...) when it cancels or
        // reschedules itself.
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(SynchronousExecutor())
                .setTaskExecutor(SynchronousExecutor())
                .build(),
        )

        // The notification path reads this channel; create it so the test reaches the same code
        // path as production startup.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HrApp.CASE_UPDATES_CHANNEL_ID, "case updates",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService<NotificationManager>()!!.createNotificationChannel(channel)
        }
    }

    @Test fun `toggle off returns success without touching repo`() = runTest {
        prefs.setCaseNotificationsEnabled(false)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertActiveNotifications(0)
    }

    @Test fun `no persisted user returns success without touching repo`() = runTest {
        prefs.setCaseNotificationsEnabled(true)
        // no user saved

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertActiveNotifications(0)
    }

    @Test fun `no change reschedules and posts no notification`() = runTest {
        prefs.saveUser(sampleUser(id = 7))
        prefs.setCaseNotificationsEnabled(true)
        prefs.saveCaseSnapshot(mapOf(1 to "OPEN"))
        coEvery { repo.getUserCases(7) } returns ApiResult.Success(
            listOf(sampleCase(id = 1, status = "OPEN"))
        )

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertActiveNotifications(0)
        // Pref remains enabled (chain continues).
        assertThat(prefs.isCaseNotificationsEnabled()).isTrue()
    }

    @Test fun `change detected posts notification and stops chain`() = runTest {
        prefs.saveUser(sampleUser(id = 7))
        prefs.setCaseNotificationsEnabled(true)
        prefs.saveCaseSnapshot(mapOf(1 to "OPEN"))
        coEvery { repo.getUserCases(7) } returns ApiResult.Success(
            listOf(sampleCase(id = 1, status = "CLOSED")) // status flipped
        )

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(prefs.isCaseNotificationsEnabled()).isFalse()
        assertThat(prefs.getCaseSnapshot()).isEmpty()
        assertActiveNotifications(1)
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private fun buildWorker(): CaseUpdateWorker =
        TestListenableWorkerBuilder<CaseUpdateWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repo, prefs))
            .build()

    private fun assertActiveNotifications(expected: Int) {
        val nm = context.getSystemService<NotificationManager>()!!
        assertThat(shadowOf(nm).activeNotifications.size).isEqualTo(expected)
    }

    private fun sampleUser(id: Int) = UserProfile(
        id = id, firstName = "F", lastName = "L",
        phoneNumber = "+1", email = "e@e.test", accountStatus = "ACTIVE",
        vehicles = emptyList(), createdAt = "2024-01-01",
    )

    private fun sampleCase(id: Int, status: String) = CaseProfile(
        id = id, userName = "F", vehicleInfo = "v",
        latitude = 0.0, longitude = 0.0,
        status = status, assignedEmployeeName = null,
        createdAt = "2024-01-01",
    )

    private class TestWorkerFactory(
        private val repo: UserRepository,
        private val prefs: AuthPreferences,
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker? = CaseUpdateWorker(appContext, workerParameters, repo, prefs)
    }
}
