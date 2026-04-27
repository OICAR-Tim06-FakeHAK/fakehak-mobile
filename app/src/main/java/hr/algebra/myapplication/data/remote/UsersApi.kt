package hr.algebra.myapplication.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UsersApi {


    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>


    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/cases")
    suspend fun createCase(
        @Header("Authorization") token: String,
        @Body request: CreateCaseRequest
    ): Response<CaseResponse>

    @POST("api/users/{userId}/vehicles")
    suspend fun createVehicle(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String,
        @Body request: VehicleRequest
    ): Response<Vehicle>

    @GET("api/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserResponse>
}
