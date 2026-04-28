package hr.algebra.myapplication.models

data class LoginResponse(
    val token: String,
    val role: String
)