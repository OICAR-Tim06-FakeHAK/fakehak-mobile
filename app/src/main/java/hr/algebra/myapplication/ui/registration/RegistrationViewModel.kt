package hr.algebra.myapplication.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hr.algebra.myapplication.data.RegisterResult
import hr.algebra.myapplication.data.UserRepository
import hr.algebra.myapplication.data.remote.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel = holds screen state + talks to the repository.
 *
 * Why we need it:
 * - Keeps network calls out of Composables
 * - Survives configuration changes (rotation)
 * - Gives us a single place for validation and error mapping
 */
class RegistrationViewModel(
    private val repository: UserRepository = UserRepository(),
) : ViewModel() {

    data class FormState(
        val firstName: String = "",
        val lastName: String = "",
        val phoneNumber: String = "",
        val email: String = "",
        val password: String = "",
    ) {
        fun isValid(): Boolean {
            return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                '@' in email &&
                password.length >= 6
        }
    }

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    fun onFirstNameChange(value: String) = _formState.update { it.copy(firstName = value) }
    fun onLastNameChange(value: String) = _formState.update { it.copy(lastName = value) }
    fun onPhoneNumberChange(value: String) = _formState.update { it.copy(phoneNumber = value) }
    fun onEmailChange(value: String) = _formState.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _formState.update { it.copy(password = value) }

    fun dismissMessage() {
        // Back to idle state after showing success/error.
        _uiState.value = RegistrationUiState.Idle
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }

    fun register() {
        val form = _formState.value

        if (!form.isValid()) {
            _uiState.value = RegistrationUiState.Error(
                "Please fill in all fields. Use a valid email and a password of at least 6 characters."
            )
            return
        }

        // Prevent double clicks.
        if (_uiState.value == RegistrationUiState.Loading) return

        _uiState.value = RegistrationUiState.Loading

        viewModelScope.launch {
            val request = RegisterRequest(
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                phoneNumber = form.phoneNumber.trim(),
                email = form.email.trim(),
                password = form.password,
            )

            when (val result = repository.register(request)) {
                is RegisterResult.Success -> {
                    _uiState.value = RegistrationUiState.Success
                    // Optional: clear the form on success.
                    _formState.value = FormState()
                    _navigateToLogin.value = true
                }

                is RegisterResult.HttpError -> {
                    _uiState.value = RegistrationUiState.Error(
                        "Server rejected registration (${result.code}). ${result.message}"
                    )
                }

                is RegisterResult.NetworkError -> {
                    _uiState.value = RegistrationUiState.Error(
                        "Can't reach server. Make sure the backend is running and you're using http://10.0.2.2:8080 from the emulator."
                    )
                }

                is RegisterResult.UnknownError -> {
                    _uiState.value = RegistrationUiState.Error(
                        "Unexpected error: ${result.cause.message ?: result.cause::class.java.simpleName}"
                    )
                }
            }
        }
    }
}
