package hr.algebra.myapplication.ui.case

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.algebra.myapplication.data.CreateCaseResult
import hr.algebra.myapplication.data.UserRepository
import hr.algebra.myapplication.data.remote.CreateCaseRequest
import kotlinx.coroutines.launch

class CaseViewModel(
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var uiState by mutableStateOf(CaseUiState())
        private set

    fun createCase(
        token: String,
        userId: Long,
        vehicleId: Long,
        latitude: Double,
        longitude: Double
    ) {
        if (uiState.isLoading) return

        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            val result = userRepository.createCase(
                token,
                CreateCaseRequest(
                    userId = userId,
                    vehicleId = vehicleId,
                    latitude = latitude,
                    longitude = longitude,
                    description = "Case created from mobile app"
                )
            )
            uiState = when (result) {
                is CreateCaseResult.Success -> uiState.copy(
                    isLoading = false,
                    error = null,
                    caseResponse = result.caseResponse
                )
                is CreateCaseResult.HttpError -> uiState.copy(
                    isLoading = false,
                    error = "Error ${result.code}: ${result.message}"
                )
                is CreateCaseResult.NetworkError -> uiState.copy(
                    isLoading = false,
                    error = "Network error: ${result.cause.message}"
                )
                is CreateCaseResult.UnknownError -> uiState.copy(
                    isLoading = false,
                    error = "Unknown error: ${result.cause.message}"
                )
            }
        }
    }
}

