package hr.algebra.myapplication.data.remote

import com.google.gson.annotations.SerializedName

data class Vehicle(
    val id: Long,
    val brand: String,
    val model: String,
    val vin: String,
    @SerializedName("registrationPlate") val registrationPlate: String,
    @SerializedName("firstRegistrationDate") val firstRegistrationDate: String
)

data class VehicleRequest(
    val brand: String,
    val model: String,
    val vin: String,
    @SerializedName("registrationPlate") val registrationPlate: String,
    @SerializedName("firstRegistrationDate") val firstRegistrationDate: String
)


