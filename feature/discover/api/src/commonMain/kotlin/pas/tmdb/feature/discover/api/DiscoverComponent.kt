package pas.tmdb.feature.discover.api

import com.arkivanov.decompose.value.Value
import pas.tmdb.auth.api.SessionState

interface DiscoverComponent {
    val model: Value<DiscoverModel>
    fun onIntent(intent: DiscoverIntent)
}

data class DiscoverModel(
    val movies: List<MovieItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val sessionState: SessionState = SessionState.Guest,
)

sealed interface DiscoverIntent {
    data object Load : DiscoverIntent
    data object Refresh : DiscoverIntent
    data object LoadMore : DiscoverIntent
    data class OnMovieClick(val movieId: Int) : DiscoverIntent
}
