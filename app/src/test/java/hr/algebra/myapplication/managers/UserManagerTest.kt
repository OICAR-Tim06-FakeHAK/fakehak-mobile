package hr.algebra.myapplication.managers

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hr.algebra.myapplication.data.FakeAuthPreferences
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.models.VehicleProfile
import hr.algebra.myapplication.repository.UserRepository
import hr.algebra.myapplication.repository.VehicleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserManagerTest {

    private val userRepo = mockk<UserRepository>()
    private val vehicleRepo = mockk<VehicleRepository>()

    @Test fun `constructor hydrates flow from prefs`() = runTest {
        val saved = sampleUser(id = 7)
        val prefs = FakeAuthPreferences(user = saved)

        val manager = UserManager(userRepo, vehicleRepo, prefs)

        assertThat(manager.current).isEqualTo(saved)
        assertThat(manager.currentUserId).isEqualTo(7)
    }

    @Test fun `load on success updates flow and persists`() = runTest {
        val prefs = FakeAuthPreferences()
        val manager = UserManager(userRepo, vehicleRepo, prefs)
        val fresh = sampleUser(id = 7, firstName = "Fresh")
        coEvery { userRepo.getUserProfile() } returns ApiResult.Success(fresh)

        manager.userFlow.test {
            assertThat(awaitItem()).isNull() // initial

            val result = manager.load()

            assertThat(result).isInstanceOf(ApiResult.Success::class.java)
            assertThat(awaitItem()).isEqualTo(fresh)
            cancelAndConsumeRemainingEvents()
        }

        assertThat(prefs.getUser()).isEqualTo(fresh)
    }

    @Test fun `load on error leaves flow alone`() = runTest {
        val prefs = FakeAuthPreferences(user = sampleUser(id = 7))
        val manager = UserManager(userRepo, vehicleRepo, prefs)
        coEvery { userRepo.getUserProfile() } returns ApiResult.Error(message = "nope")

        val result = manager.load()

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        assertThat(manager.current?.id).isEqualTo(7)
    }

    @Test fun `update without loaded user returns Error`() = runTest {
        val manager = UserManager(userRepo, vehicleRepo, FakeAuthPreferences())

        val result = manager.update(UserProfileUpdate("F", "L", "+1", "e@e.test"))

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
    }

    @Test fun `deleteVehicle filters local list without refetching`() = runTest {
        val current = sampleUser(
            id = 7,
            vehicles = listOf(
                vehicle(id = 1, brand = "BMW"),
                vehicle(id = 2, brand = "Audi"),
            ),
        )
        val prefs = FakeAuthPreferences(user = current)
        val manager = UserManager(userRepo, vehicleRepo, prefs)
        coEvery { vehicleRepo.deleteVehicle(7, 1) } returns ApiResult.Success(Unit)

        val result = manager.deleteVehicle(vehicleId = 1)

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat(manager.current?.vehicles?.map { it.id }).containsExactly(2)
        coVerify(exactly = 0) { userRepo.getUserProfile() } // no refetch
    }

    @Test fun `clear resets flow, wipes prefs, and emits Logout`() = runTest {
        val prefs = FakeAuthPreferences(user = sampleUser(7), token = "abc")
        val manager = UserManager(userRepo, vehicleRepo, prefs)

        // AppEventBus is a hot SharedFlow with no replay buffer — subscribe FIRST via Turbine
        // so the Logout emission has somewhere to land.
        AppEventBus.events.test {
            manager.clear()

            assertThat(awaitItem()).isInstanceOf(AppEvent.Logout::class.java)
            cancelAndConsumeRemainingEvents()
        }

        assertThat(manager.current).isNull()
        assertThat(prefs.getToken()).isNull()
        assertThat(prefs.getUser()).isNull()
    }

    // ─── fixtures ────────────────────────────────────────────────────────────

    private fun sampleUser(
        id: Int,
        firstName: String = "F",
        vehicles: List<VehicleProfile> = emptyList(),
    ) = UserProfile(
        id = id, firstName = firstName, lastName = "L",
        phoneNumber = "+1", email = "e@e.test", accountStatus = "ACTIVE",
        vehicles = vehicles, createdAt = "2024-01-01",
    )

    private fun vehicle(id: Int, brand: String) =
        VehicleProfile(id, brand, "X", "VIN$id", "PL$id", "2024-01-01", "2024-01-01")
}
