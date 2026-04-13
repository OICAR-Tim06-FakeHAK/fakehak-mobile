package hr.algebra.myapplication.ui.login

data class User(val id: Long, val username: String, val email: String)

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val token: String, val user: User) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

