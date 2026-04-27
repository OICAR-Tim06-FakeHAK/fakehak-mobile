package hr.algebra.myapplication.data.remote

data class CreateCaseRequest(
    val userId: Long,
    val vehicleId: Long,
    val latitude: Double,
    val longitude: Double,
    val description: String?
)

