package hr.algebra.myapplication.repository

import hr.algebra.myapplication.data.AuthPreferences
import hr.algebra.myapplication.managers.TokenManager
import hr.algebra.myapplication.managers.UserManager
import hr.algebra.myapplication.models.ApiResult
import hr.algebra.myapplication.models.LoginRequest
import hr.algebra.myapplication.models.RegisterRequest
import hr.algebra.myapplication.models.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val prefs: AuthPreferences,
) {
    suspend fun login(request: LoginRequest): ApiResult<UserProfile> {
        return when (val loginResult = userRepository.login(request)) {
            is ApiResult.Success -> {
                tokenManager.saveToken(loginResult.data.token)
                when (val loadResult = userManager.load()) {
                    is ApiResult.Success -> loadResult
                    is ApiResult.Error -> {
                        tokenManager.clear()
                        loadResult
                    }
                    is ApiResult.Loading -> loadResult
                }
            }
            is ApiResult.Error -> loginResult
            is ApiResult.Loading -> loginResult
        }
    }

    suspend fun register(request: RegisterRequest): ApiResult<Unit> =
        userRepository.register(request)

    suspend fun logout() = userManager.clear()

    fun hasPersistedSession(): Boolean =
        tokenManager.isTokenValid() && prefs.getUser() != null
}
