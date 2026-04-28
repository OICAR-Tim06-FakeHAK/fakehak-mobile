package hr.algebra.myapplication.api

import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.LoginResponse
import hr.algebra.myapplication.models.RegisterRequest
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.models.VehicleProfile
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // ─── User ─────────────────────────────────────────────────────────────────

    @GET("users/me")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UserProfileUpdate
    ): Response<UserProfile>

    // ─── Vehicles ─────────────────────────────────────────────────────────────

    @GET("users/me/vehicles")
    suspend fun getVehicles(): Response<List<VehicleProfile>>

    @POST("users/me/vehicles")
    suspend fun addVehicle(
        @Body vehicle: VehicleProfile
    ): Response<VehicleProfile>

    @PUT("users/me/vehicles/{vehicleId}")
    suspend fun updateVehicle(
        @Path("vehicleId") vehicleId: Int,
        @Body vehicle: VehicleProfile
    ): Response<VehicleProfile>

    @DELETE("users/{userId}/vehicles/{vehicleId}")
    suspend fun deleteVehicle(
        @Path("userId") userId: Int,
        @Path("vehicleId") vehicleId: Int
    ): Response<Unit>
}