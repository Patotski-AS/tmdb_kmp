package pas.tmdb.feature.discover.impl.store

import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.discover.api.MovieItemModel

data class DiscoverState(
    val movies: List<MovieItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val endReached: Boolean = false,
    val sessionState: SessionState = SessionState.Guest,
)
