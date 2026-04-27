package hr.algebra.myapplication.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.algebra.myapplication.data.CreateVehicleResult
import hr.algebra.myapplication.data.UserRepository
import hr.algebra.myapplication.data.remote.VehicleRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    fun createVehicle(userId: Long, token: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = userRepository.createVehicle(
                userId,
                token,
                VehicleRequest(
                    brand = _uiState.value.brand,
                    model = _uiState.value.model,
                    vin = _uiState.value.vin,
                    registrationPlate = _uiState.value.registrationPlate,
                    firstRegistrationDate = _uiState.value.firstRegistrationDate
                )
            )
            _uiState.update {
                when (result) {
                    is CreateVehicleResult.Success -> it.copy(
                        isLoading = false,
                        error = null,
                        vehicle = result.vehicle
                    )
                    is CreateVehicleResult.HttpError -> it.copy(
                        isLoading = false,
                        error = "Error ${result.code}: ${result.message}"
                    )
                    is CreateVehicleResult.NetworkError -> it.copy(
                        isLoading = false,
                        error = "Network error: ${result.cause.message}"
                    )
                    is CreateVehicleResult.UnknownError -> it.copy(
                        isLoading = false,
                        error = "Unknown error: ${result.cause.message}"
                    )
                }
            }
        }
    }

    fun onBrandChange(brand: String) {
        _uiState.update { it.copy(brand = brand) }
    }

    fun onModelChange(model: String) {
        _uiState.update { it.copy(model = model) }
    }

    fun onVinChange(vin: String) {
        _uiState.update { it.copy(vin = vin) }
    }

    fun onRegistrationPlateChange(registrationPlate: String) {
        _uiState.update { it.copy(registrationPlate = registrationPlate) }
    }

    fun onFirstRegistrationDateChange(firstRegistrationDate: String) {
        _uiState.update { it.copy(firstRegistrationDate = firstRegistrationDate) }
    }
}

