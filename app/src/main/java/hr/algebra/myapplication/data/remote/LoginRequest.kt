package hr.algebra.myapplication.data.remote


data class LoginRequest(
    val identifier: String,
    val password: String,
)

