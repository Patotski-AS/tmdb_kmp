package pas.tmdb.feature.details.impl.store

sealed interface DetailsLabel {
    data object NavigateBack : DetailsLabel
}
