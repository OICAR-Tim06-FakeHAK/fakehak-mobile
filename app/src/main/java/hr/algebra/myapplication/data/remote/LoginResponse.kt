package hr.algebra.myapplication.data.remote


data class LoginResponse(
    val token: String,
    val role: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String
)
