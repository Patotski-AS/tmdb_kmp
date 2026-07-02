package pas.tmdb.auth.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pas.tmdb.auth.api.AuthResultHandler
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState
import pas.tmdb.data.storage.api.SessionStorage

class SessionManager(
    private val sessionStorage: SessionStorage,
) : SessionRepository, AuthResultHandler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var cachedSessionId: String? = null

    init {
        scope.launch {
            restoreSession()
        }
    }

    override suspend fun getSessionId(): String? {
        cachedSessionId?.let { return it }
        return sessionStorage.loadSessionId()?.also { cachedSessionId = it }
    }

    override suspend fun logout() {
        sessionStorage.clear()
        cachedSessionId = null
        _sessionState.value = SessionState.Guest
    }

    override suspend fun onLoginSuccess(sessionId: String, accountId: Int, username: String?) {
        sessionStorage.saveSession(
            sessionId = sessionId,
            accountId = accountId,
            username = username,
        )
        cachedSessionId = sessionId
        _sessionState.value = SessionState.Authenticated(
            accountId = accountId,
            username = username,
        )
    }

    private suspend fun restoreSession() {
        val sessionId = sessionStorage.loadSessionId() ?: return
        val accountId = sessionStorage.loadAccountId() ?: return

        cachedSessionId = sessionId
        _sessionState.value = SessionState.Authenticated(
            accountId = accountId,
            username = sessionStorage.loadUsername(),
        )
    }
}
