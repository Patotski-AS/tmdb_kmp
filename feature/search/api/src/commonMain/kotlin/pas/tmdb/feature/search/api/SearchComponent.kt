package pas.tmdb.feature.search.api

import com.arkivanov.decompose.value.Value

interface SearchComponent {
    val model: Value<SearchModel>
    fun onIntent(intent: SearchIntent)
}

data class SearchModel(
    val query: String = "",
    val movies: List<MovieItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val hasSearched: Boolean = false,
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Search : SearchIntent
    data object Clear : SearchIntent
    data object LoadMore : SearchIntent
    data class OnMovieClick(val movieId: Int) : SearchIntent
}
