package pas.tmdb.feature.search.impl.store

sealed interface SearchLabel {
    data class NavigateToDetails(val movieId: Int) : SearchLabel
    data class ShowError(val message: String) : SearchLabel
}
