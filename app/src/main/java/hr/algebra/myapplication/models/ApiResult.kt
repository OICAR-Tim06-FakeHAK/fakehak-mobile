package hr.algebra.myapplication.models

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val apiError: ApiError? = null, val message: String? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}