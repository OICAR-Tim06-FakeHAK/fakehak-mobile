package hr.algebra.myapplication.models;

import com.google.gson.annotations.SerializedName

data class ApiError (
    val timestamp: String,
    val status: Int,
    @SerializedName("Conflict") val conflict: String?,
    val message: String
)
