package pas.tmdb.feature.media.impl.store

sealed interface MediaLabel {
    data class NavigateToDetails(val movieId: Int) : MediaLabel
    data object NavigateToProfile : MediaLabel
    data class ShowError(val message: String) : MediaLabel
}
