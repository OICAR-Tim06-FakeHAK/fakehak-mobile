package hr.algebra.myapplication.data.remote


data class LoginResponse(
    val token: String,
    val role: String,
)
