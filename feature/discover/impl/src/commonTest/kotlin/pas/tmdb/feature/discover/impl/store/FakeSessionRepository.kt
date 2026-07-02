package pas.tmdb.feature.discover.impl.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.SessionState

internal class FakeSessionRepository : SessionRepository {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = _sessionState

    override suspend fun getSessionId(): String? = null

    override suspend fun logout() {
        _sessionState.value = SessionState.Guest
    }

    fun setSessionState(state: SessionState) {
        _sessionState.value = state
    }
}
