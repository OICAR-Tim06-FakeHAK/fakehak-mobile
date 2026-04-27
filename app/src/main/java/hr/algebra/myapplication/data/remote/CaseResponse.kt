package hr.algebra.myapplication.data.remote

import com.google.gson.annotations.SerializedName

data class CaseResponse(
    @SerializedName("id")
    val caseId: Long,
    val status: String,
    val createdAt: String
)

