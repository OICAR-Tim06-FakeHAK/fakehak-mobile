package hr.algebra.myapplication.models

data class CaseReport (
    /*
    {
    "userId": 0,
    "vehicleId": 0,
    "latitude": 0.1,
    "longitude": 0.1,
    "description": "string"
    }
     */
    val userId: Int,
    val vehicleId: Int,
    val latitude: Double,
    val longitude: Double,
    val description: String
)