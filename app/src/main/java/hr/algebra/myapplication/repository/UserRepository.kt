package hr.algebra.myapplication.repository

import com.google.gson.Gson
import hr.algebra.myapplication.api.ApiService
import hr.algebra.myapplication.models.ApiError
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.LoginResponse
import hr.algebra.myapplication.models.RegisterRequest
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.models.VehicleProfile
import retrofit2.Response

class UserRepository(
    private val api: ApiService
) {
    suspend fun register(request: RegisterRequest): ApiResult<Unit> {
        return safeApiCall { api.register(request) }
    }

    suspend fun login(request: LoginRequest): ApiResult<LoginResponse> {
        return safeApiCall { api.login(request) }
    }

    suspend fun getUserProfile(): ApiResult<UserProfile> {
        return safeApiCall { api.getUserProfile() }
    }

    suspend fun updateUser(id: Int, request: UserProfileUpdate): ApiResult<UserProfile> {
        return safeApiCall { api.updateUser(id, request) }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    ApiResult.Success(Unit as T)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val apiError = try {
                    Gson().fromJson(errorBody, ApiError::class.java)
                } catch (e: Exception) {
                    null
                }
                ApiResult.Error(apiError = apiError, message = errorBody)
            }
        } catch (e: Exception) {
            ApiResult.Error(message = e.localizedMessage ?: "Unknown error occurred")
        }
    }
}