package pas.tmdb.feature.search.impl.store

import pas.tmdb.feature.search.api.MovieItemModel

data class SearchState(
    val query: String = "",
    val movies: List<MovieItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val endReached: Boolean = false,
    val hasSearched: Boolean = false,
)
