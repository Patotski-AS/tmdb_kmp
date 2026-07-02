package pas.tmdb.feature.details.impl.store

sealed interface DetailsLabel {
    data object NavigateBack : DetailsLabel
    data object NavigateToLogin : DetailsLabel
    data class ShowError(val message: String) : DetailsLabel
}
