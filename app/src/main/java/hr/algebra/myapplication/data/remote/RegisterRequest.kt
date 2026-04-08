package hr.algebra.myapplication.data.remote


data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
)

