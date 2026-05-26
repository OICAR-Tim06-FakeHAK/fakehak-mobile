package hr.algebra.myapplication.managers

import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.CaseProfile
import hr.algebra.myapplication.models.CaseReport
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.models.VehicleProfile
import hr.algebra.myapplication.repository.UserRepository
import hr.algebra.myapplication.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val prefs: AuthPreferences,
) {
    private val _userFlow = MutableStateFlow<UserProfile?>(prefs.getUser())

    val userFlow: StateFlow<UserProfile?> = _userFlow.asStateFlow()

    val current: UserProfile? get() = _userFlow.value

    val currentUserId: Int? get() = _userFlow.value?.id

    suspend fun load(): ApiResult<UserProfile> {
        return when (val result = userRepository.getUserProfile()) {
            is ApiResult.Success -> {
                setInternal(result.data)
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun update(request: UserProfileUpdate): ApiResult<UserProfile> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = userRepository.updateUser(id, request)) {
            is ApiResult.Success -> {
                setInternal(result.data)
                AppEventBus.publish(AppEvent.UserUpdated(result.data))
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createReport(caseReport: CaseReport): ApiResult<Unit> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = userRepository.createReport(id, caseReport)) {
            is ApiResult.Success -> result
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getCases(): ApiResult<List<CaseProfile>> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")
        return userRepository.getUserCases(id)
    }

    suspend fun addVehicle(vehicle: VehicleProfile): ApiResult<VehicleProfile> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = vehicleRepository.addVehicle(id, vehicle)) {
            is ApiResult.Success -> {
                load()
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateVehicle(vehicleId: Int, vehicle: VehicleProfile): ApiResult<VehicleProfile> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = vehicleRepository.updateVehicle(id, vehicleId, vehicle)) {
            is ApiResult.Success -> {
                load()
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteVehicle(vehicleId: Int): ApiResult<Unit> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = vehicleRepository.deleteVehicle(id, vehicleId)) {
            is ApiResult.Success -> {
                val updated = _userFlow.value?.let { profile ->
                    profile.copy(vehicles = profile.vehicles.filterNot { it.id == vehicleId })
                }
                if (updated != null) {
                    setInternal(updated)
                    AppEventBus.publish(AppEvent.UserUpdated(updated))
                }
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    fun set(profile: UserProfile) = setInternal(profile)

    private fun setInternal(profile: UserProfile) {
        _userFlow.value = profile
        prefs.saveUser(profile)
    }

    suspend fun clear() {
        _userFlow.value = null
        prefs.clearAll()
        AppEventBus.publish(AppEvent.Logout)
    }
}
