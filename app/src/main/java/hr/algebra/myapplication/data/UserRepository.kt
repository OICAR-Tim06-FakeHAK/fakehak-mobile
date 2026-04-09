package hr.algebra.myapplication.data

import hr.algebra.myapplication.data.remote.ApiClient
import hr.algebra.myapplication.data.remote.RegisterRequest
import java.io.IOException

class UserRepository(
    // Default to the singleton, but allow injection for tests later.
    private val api: hr.algebra.myapplication.data.remote.UsersApi = ApiClient.usersApi,
) {

    suspend fun register(request: RegisterRequest): RegisterResult {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                RegisterResult.Success
            } else {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                RegisterResult.HttpError(
                    code = response.code(),
                    message = errorBody ?: response.message().ifBlank { "Registration failed." }
                )
            }
        } catch (e: IOException) {
            // Typical when server isn't reachable, timeout, no internet, etc.
            RegisterResult.NetworkError(e)
        } catch (t: Throwable) {
            RegisterResult.UnknownError(t)
        }
    }

    suspend fun login(request: hr.algebra.myapplication.data.remote.LoginRequest): LoginResult {
        return try {
            val response = api.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    LoginResult.UnknownError(IllegalStateException("Empty login response"))
                } else if (body.token.isBlank()) {
                    LoginResult.UnknownError(IllegalStateException("Login response token is blank"))
                } else {
                    LoginResult.Success(body)
                }
            } else {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                LoginResult.HttpError(
                    code = response.code(),
                    message = errorBody ?: response.message().ifBlank { "Login failed." }
                )
            }
        } catch (e: IOException) {
            LoginResult.NetworkError(e)
        } catch (t: Throwable) {
            LoginResult.UnknownError(t)
        }
    }
}

sealed interface RegisterResult {
    data object Success : RegisterResult


    data class HttpError(val code: Int, val message: String) : RegisterResult


    data class NetworkError(val cause: IOException) : RegisterResult

    data class UnknownError(val cause: Throwable) : RegisterResult
}

sealed interface LoginResult {
    data class Success(val response: hr.algebra.myapplication.data.remote.LoginResponse) : LoginResult
    data class HttpError(val code: Int, val message: String) : LoginResult
    data class NetworkError(val cause: IOException) : LoginResult
    data class UnknownError(val cause: Throwable) : LoginResult
}
