package hr.algebra.myapplication.models;

data class UserProfile(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val accountStatus: String,
    val vehicles: List<VehicleProfile>,
    val createdAt: String
)
