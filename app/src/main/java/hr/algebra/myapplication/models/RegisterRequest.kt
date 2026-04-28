package hr.algebra.myapplication.models

data class RegisterRequest (
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val password: String
)