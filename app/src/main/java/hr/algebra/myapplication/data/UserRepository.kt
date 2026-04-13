package hr.algebra.myapplication.data

import hr.algebra.myapplication.data.remote.ApiClient
import hr.algebra.myapplication.data.remote.LoginRequest
import hr.algebra.myapplication.data.remote.LoginResponse
import hr.algebra.myapplication.data.remote.RegisterRequest
import hr.algebra.myapplication.data.remote.Vehicle
import hr.algebra.myapplication.data.remote.VehicleRequest
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

    suspend fun login(request: LoginRequest): LoginResult {
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

    suspend fun createVehicle(
        userId: Long,
        token: String,
        request: VehicleRequest
    ): CreateVehicleResult {
        return try {
            val response = api.createVehicle(userId, "Bearer $token", request)
            if (response.isSuccessful) {
                val vehicle = response.body()
                if (vehicle == null) {
                    CreateVehicleResult.UnknownError(IllegalStateException("Empty vehicle response"))
                } else {
                    CreateVehicleResult.Success(vehicle)
                }
            } else {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                CreateVehicleResult.HttpError(
                    code = response.code(),
                    message = errorBody ?: response.message().ifBlank { "Vehicle creation failed." }
                )
            }
        } catch (e: IOException) {
            CreateVehicleResult.NetworkError(e)
        } catch (t: Throwable) {
            CreateVehicleResult.UnknownError(t)
        }
    }

    suspend fun getMe(token: String): GetMeResult {
        return try {
            val response = api.getMe("Bearer $token")
            if (response.isSuccessful) {
                val user = response.body()
                if (user == null) {
                    GetMeResult.UnknownError(IllegalStateException("Empty user response"))
                } else {
                    GetMeResult.Success(user)
                }
            } else {
                val errorBody = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                GetMeResult.HttpError(
                    code = response.code(),
                    message = errorBody ?: response.message().ifBlank { "User fetch failed." }
                )
            }
        } catch (e: IOException) {
            GetMeResult.NetworkError(e)
        } catch (t: Throwable) {
            GetMeResult.UnknownError(t)
        }
    }
}

sealed class RegisterResult {
    data object Success : RegisterResult()
    data class HttpError(val code: Int, val message: String) : RegisterResult()
    data class NetworkError(val cause: Throwable) : RegisterResult()
    data class UnknownError(val cause: Throwable) : RegisterResult()
}

sealed class LoginResult {
    data class Success(val response: LoginResponse) : LoginResult()
    data class HttpError(val code: Int, val message: String) : LoginResult()
    data class NetworkError(val cause: Throwable) : LoginResult()
    data class UnknownError(val cause: Throwable) : LoginResult()
}

sealed class CreateVehicleResult {
    data class Success(val vehicle: Vehicle) : CreateVehicleResult()
    data class HttpError(val code: Int, val message: String) : CreateVehicleResult()
    data class NetworkError(val cause: Throwable) : CreateVehicleResult()
    data class UnknownError(val cause: Throwable) : CreateVehicleResult()
}

sealed class GetMeResult {
    data class Success(val user: hr.algebra.myapplication.data.remote.UserResponse) : GetMeResult()
    data class HttpError(val code: Int, val message: String) : GetMeResult()
    data class NetworkError(val cause: Throwable) : GetMeResult()
    data class UnknownError(val cause: Throwable) : GetMeResult()
}
