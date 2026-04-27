package hr.algebra.myapplication.ui.case

import hr.algebra.myapplication.data.remote.CaseResponse

data class CaseUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val caseResponse: CaseResponse? = null
)

