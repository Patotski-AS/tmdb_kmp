package pas.tmdb.feature.login.impl.store

import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.login.api.FavoriteMovieModel

data class LoginState(
    val sessionState: SessionState = SessionState.Guest,
    val isLoading: Boolean = false,
    val isAwaitingApproval: Boolean = false,
    val pendingRequestToken: String? = null,
    val error: String? = null,
    val favorites: List<FavoriteMovieModel> = emptyList(),
    val isFavoritesLoading: Boolean = false,
    val favoritesError: String? = null,
)
