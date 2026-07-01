package pas.tmdb.feature.discover.impl.store

sealed interface DiscoverLabel {
    data class NavigateToDetails(val movieId: Int) : DiscoverLabel
    data class ShowError(val message: String) : DiscoverLabel
}
