package hr.algebra.myapplication.api

import hr.algebra.myapplication.models.CaseProfile
import hr.algebra.myapplication.models.CaseReport
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.LoginResponse
import hr.algebra.myapplication.models.RegisterRequest
import hr.algebra.myapplication.models.UserProfile
import hr.algebra.myapplication.models.UserProfileUpdate
import hr.algebra.myapplication.models.VehicleProfile
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/users/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("api/users/me")
    suspend fun getUserProfile(): Response<UserProfile>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UserProfileUpdate
    ): Response<UserProfile>

    @GET("api/users/{userId}/vehicles")
    suspend fun getVehicles(
        @Path("userId") userId: Int,
    ): Response<List<VehicleProfile>>

    @POST("api/users/{userId}/vehicles")
    suspend fun addVehicle(
        @Path("userId") userId: Int,
        @Body vehicle: VehicleProfile
    ): Response<VehicleProfile>

    @PUT("api/users/{userId}/vehicles/{vehicleId}")
    suspend fun updateVehicle(
        @Path("userId") userId: Int,
        @Path("vehicleId") vehicleId: Int,
        @Body vehicle: VehicleProfile
    ): Response<VehicleProfile>

    @DELETE("api/users/{userId}/vehicles/{vehicleId}")
    suspend fun deleteVehicle(
        @Path("userId") userId: Int,
        @Path("vehicleId") vehicleId: Int
    ): Response<Unit>

    @POST("/api/cases")
    suspend fun createCase(
        @Body caseReport: CaseReport
    ): Response<Unit>

    @GET("/api/cases/user/{userId}")
    suspend fun getUserCases(
        @Path("userId") userId: Int
    ): Response<List<CaseProfile>>
}