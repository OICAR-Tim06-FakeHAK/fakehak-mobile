package hr.algebra.myapplication.ui.vehicle

import hr.algebra.myapplication.data.remote.Vehicle

data class VehicleUiState(
    val brand: String = "",
    val model: String = "",
    val vin: String = "",
    val registrationPlate: String = "",
    val firstRegistrationDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val vehicle: Vehicle? = null
)

