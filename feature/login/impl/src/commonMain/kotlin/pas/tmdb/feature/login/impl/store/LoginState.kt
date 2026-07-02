package pas.tmdb.feature.login.impl.store

data class LoginState(
    val sessionState: pas.tmdb.auth.api.SessionState = pas.tmdb.auth.api.SessionState.Guest,
    val isLoading: Boolean = false,
    val isAwaitingApproval: Boolean = false,
    val pendingRequestToken: String? = null,
    val error: String? = null,
)
