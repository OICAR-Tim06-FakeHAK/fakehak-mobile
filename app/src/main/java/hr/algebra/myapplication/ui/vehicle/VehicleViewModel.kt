package hr.algebra.myapplication.ui.vehicle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.algebra.myapplication.data.CreateVehicleResult
import hr.algebra.myapplication.data.UserRepository
import hr.algebra.myapplication.data.remote.VehicleRequest
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var uiState by mutableStateOf(VehicleUiState())
        private set

    fun createVehicle(userId: Long, token: String) {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            val result = userRepository.createVehicle(
                userId,
                token,
                VehicleRequest(
                    brand = uiState.brand,
                    model = uiState.model,
                    vin = uiState.vin,
                    registrationPlate = uiState.registrationPlate,
                    firstRegistrationDate = uiState.firstRegistrationDate
                )
            )
            uiState = when (result) {
                is CreateVehicleResult.Success -> uiState.copy(
                    isLoading = false,
                    error = null,
                    vehicle = result.vehicle
                )
                is CreateVehicleResult.HttpError -> uiState.copy(
                    isLoading = false,
                    error = "Error ${result.code}: ${result.message}"
                )
                is CreateVehicleResult.NetworkError -> uiState.copy(
                    isLoading = false,
                    error = "Network error: ${result.cause.message}"
                )
                is CreateVehicleResult.UnknownError -> uiState.copy(
                    isLoading = false,
                    error = "Unknown error: ${result.cause.message}"
                )
            }
        }
    }

    fun onBrandChange(brand: String) {
        uiState = uiState.copy(brand = brand)
    }

    fun onModelChange(model: String) {
        uiState = uiState.copy(model = model)
    }

    fun onVinChange(vin: String) {
        uiState = uiState.copy(vin = vin)
    }

    fun onRegistrationPlateChange(registrationPlate: String) {
        uiState = uiState.copy(registrationPlate = registrationPlate)
    }

    fun onFirstRegistrationDateChange(firstRegistrationDate: String) {
        uiState = uiState.copy(firstRegistrationDate = firstRegistrationDate)
    }
}

