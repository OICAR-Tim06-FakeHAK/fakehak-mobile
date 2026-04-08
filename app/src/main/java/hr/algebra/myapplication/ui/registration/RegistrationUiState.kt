package hr.algebra.myapplication.ui.registration


sealed interface RegistrationUiState {
    data object Idle : RegistrationUiState
    data object Loading : RegistrationUiState
    data object Success : RegistrationUiState

    data class Error(val message: String) : RegistrationUiState
}

