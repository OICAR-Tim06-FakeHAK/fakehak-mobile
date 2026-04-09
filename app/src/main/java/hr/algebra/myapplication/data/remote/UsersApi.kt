package hr.algebra.myapplication.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {


    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>


    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
