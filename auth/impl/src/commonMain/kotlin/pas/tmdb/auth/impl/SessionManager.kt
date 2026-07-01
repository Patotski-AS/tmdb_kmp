package pas.tmdb.auth.impl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pas.tmdb.auth.api.AuthResultHandler
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState

class SessionManager : SessionRepository, AuthResultHandler {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override suspend fun getSessionId(): String? = null

    override suspend fun logout() {
        _sessionState.value = SessionState.Guest
    }

    override suspend fun onLoginSuccess(sessionId: String, accountId: Int, username: String?) {
        _sessionState.value = SessionState.Authenticated(
            accountId = accountId,
            username = username,
        )
    }
}
