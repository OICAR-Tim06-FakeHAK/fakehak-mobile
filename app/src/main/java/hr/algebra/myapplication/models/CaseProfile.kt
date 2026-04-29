package hr.algebra.myapplication.models

data class CaseProfile (
    val id: Int,
    val userName: String,
    val vehicleInfo: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val assignedEmployeeName: String?,
    val createdAt: String
    )