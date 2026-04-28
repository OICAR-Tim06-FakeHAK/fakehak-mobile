package hr.algebra.myapplication.repository

import com.google.gson.Gson
import hr.algebra.myapplication.api.ApiService
import hr.algebra.myapplication.models.ApiError
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.VehicleProfile
import retrofit2.Response

class VehicleRepository(
    private val api: ApiService
) {
    suspend fun getVehicles(): ApiResult<List<VehicleProfile>> {
        return safeApiCall { api.getVehicles() }
    }

    suspend fun addVehicle(vehicle: VehicleProfile): ApiResult<VehicleProfile> {
        return safeApiCall { api.addVehicle(vehicle) }
    }

    suspend fun updateVehicle(vehicleId: Int, vehicle: VehicleProfile): ApiResult<VehicleProfile> {
        return safeApiCall { api.updateVehicle(vehicleId, vehicle) }
    }

    suspend fun deleteVehicle(userId: Int, vehicleId: Int): ApiResult<Unit> {
        return safeApiCall { api.deleteVehicle(userId, vehicleId) }
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