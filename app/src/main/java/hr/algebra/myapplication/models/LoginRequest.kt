package hr.algebra.myapplication.models

data class LoginRequest(
    val identifier: String,
    val password: String
)