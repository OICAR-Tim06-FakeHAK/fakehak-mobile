package hr.algebra.myapplication.managers

import hr.algebra.myapplication.repository.UserRepository
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class UserManager(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository,
    private val eventBus: AppEventBus = AppEventBus
) {

    private val _userFlow = MutableStateFlow<UserProfile?>(null)

    /** Observe the current user reactively from any ViewModel or Composable. */
    val userFlow: StateFlow<UserProfile?> = _userFlow.asStateFlow()

    /** Synchronous snapshot — null if not loaded yet. */
    val current: UserProfile? get() = _userFlow.value

    /** Convenience — the current user's id, or null if not loaded. */
    val currentUserId: Int? get() = _userFlow.value?.id

    // ─── Load ─────────────────────────────────────────────────────────────────

    /**
     * Fetches /api/users/me and caches the result.
     * Call this once after login and whenever a refresh is needed.
     */
    suspend fun load(): ApiResult<UserProfile> {
        return when (val result = userRepository.getUserProfile()) {
            is ApiResult.Success -> {
                _userFlow.value = result.data
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * PUTs updated profile data to /api/users/{id} using the cached user's id,
     * then refreshes the in-memory profile and publishes [AppEvent.UserUpdated].
     */
    suspend fun update(request: UserProfileUpdate): ApiResult<UserProfile> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = userRepository.updateUser(id, request)) {
            is ApiResult.Success -> {
                _userFlow.value = result.data
                eventBus.publish(AppEvent.UserUpdated(result.data))
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    // ─── Vehicles ─────────────────────────────────────────────────────────────

    suspend fun deleteVehicle(vehicleId: Int): ApiResult<Unit> {
        val id = currentUserId
            ?: return ApiResult.Error(message = "No authenticated user loaded")

        return when (val result = vehicleRepository.deleteVehicle(id, vehicleId)) {
            is ApiResult.Success -> {
                val updated = _userFlow.value?.let { profile ->
                    profile.copy(vehicles = profile.vehicles.filterNot { it.id == vehicleId })
                }
                _userFlow.value = updated
                updated?.let { eventBus.publish(AppEvent.UserUpdated(it)) }
                result
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    // ─── Set / Clear ──────────────────────────────────────────────────────────

    fun set(profile: UserProfile) {
        _userFlow.value = profile
    }

    suspend fun clear() {
        _userFlow.value = null
        eventBus.publish(AppEvent.Logout)
    }
}