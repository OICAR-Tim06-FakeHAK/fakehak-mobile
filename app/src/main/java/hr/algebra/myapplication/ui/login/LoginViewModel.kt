package hr.algebra.myapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    data class FormState(
        val identifier: String = "",
        val password: String = "",
    ) {
        fun isValid(): Boolean = identifier.isNotBlank() && password.isNotBlank()
    }

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _role = MutableStateFlow<String?>(null)
    val role: StateFlow<String?> = _role.asStateFlow()

    private val repository: hr.algebra.myapplication.data.UserRepository = hr.algebra.myapplication.data.UserRepository()

    fun onIdentifierChange(value: String) = _formState.update { it.copy(identifier = value) }
    fun onPasswordChange(value: String) = _formState.update { it.copy(password = value) }

    fun dismissMessage() {
        _uiState.value = LoginUiState.Idle
    }

    fun onNavigatedToHome() {
        _navigateToHome.value = false
    }

    fun login() {
        val form = _formState.value
        if (!form.isValid()) {
            _uiState.value = LoginUiState.Error("Please enter your email/username and password")
            return
        }
        if (_uiState.value == LoginUiState.Loading) return

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val request = hr.algebra.myapplication.data.remote.LoginRequest(
                identifier = form.identifier.trim(),
                password = form.password,
            )

            when (val loginResult = repository.login(request)) {
                is hr.algebra.myapplication.data.LoginResult.Success -> {
                    _authToken.value = loginResult.response.token
                    _role.value = loginResult.response.role
                    // After login, fetch user details
                    fetchUserDetails(loginResult.response.token)
                }

                is hr.algebra.myapplication.data.LoginResult.HttpError -> {
                    val message = when (loginResult.code) {
                        401 -> "Invalid credentials. Please check your identifier and password."
                        403 -> "Forbidden (403). The backend blocked this login request. This is usually a Spring Security configuration/CSRF issue, not a wrong password."
                        else -> "Login failed (${loginResult.code}). ${loginResult.message}"
                    }
                    _uiState.value = LoginUiState.Error(message)
                }

                is hr.algebra.myapplication.data.LoginResult.NetworkError -> {
                    _uiState.value = LoginUiState.Error(
                        "Can't reach server. Make sure the backend is running and you're using http://10.0.2.2:8080 from the emulator."
                    )
                }

                is hr.algebra.myapplication.data.LoginResult.UnknownError -> {
                    _uiState.value = LoginUiState.Error(
                        "Unexpected error: ${loginResult.cause.message ?: loginResult.cause::class.java.simpleName}"
                    )
                }
            }
        }
    }

    private fun fetchUserDetails(token: String) {
        viewModelScope.launch {
            when (val userResult = repository.getMe(token)) {
                is hr.algebra.myapplication.data.GetMeResult.Success -> {
                    _uiState.value = LoginUiState.Success(
                        token = token,
                        user = User(
                            id = userResult.user.id,
                            username = userResult.user.username ?: "${userResult.user.firstName ?: ""} ${userResult.user.lastName ?: ""}".trim().ifEmpty { "Unknown User" },
                            email = userResult.user.email
                        )
                    )
                    _navigateToHome.value = true
                }
                is hr.algebra.myapplication.data.GetMeResult.HttpError -> {
                    _uiState.value = LoginUiState.Error("Failed to fetch user details: ${userResult.message}")
                }
                is hr.algebra.myapplication.data.GetMeResult.NetworkError -> {
                    _uiState.value = LoginUiState.Error("Network error while fetching user details.")
                }
                is hr.algebra.myapplication.data.GetMeResult.UnknownError -> {
                    _uiState.value = LoginUiState.Error("Unknown error while fetching user details.")
                }
            }
        }
    }

    fun logout() {
        _authToken.value = null
        _role.value = null
        _formState.value = FormState()
        _uiState.value = LoginUiState.Idle
        _navigateToHome.value = false
    }
}
