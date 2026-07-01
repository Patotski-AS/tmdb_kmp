package pas.tmdb.auth.api

import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    data object Guest : SessionState
    data class Authenticated(
        val accountId: Int,
        val username: String?,
    ) : SessionState
}

interface SessionRepository {
    val sessionState: StateFlow<SessionState>
    suspend fun getSessionId(): String?
    suspend fun logout()
}

interface AuthResultHandler {
    suspend fun onLoginSuccess(sessionId: String, accountId: Int, username: String?)
}

sealed interface AuthLaunchResult {
    data class Success(val sessionId: String) : AuthLaunchResult
    data object Cancelled : AuthLaunchResult
    data class Error(val message: String) : AuthLaunchResult
}

interface AuthLauncher {
    suspend fun launch(): AuthLaunchResult
}
