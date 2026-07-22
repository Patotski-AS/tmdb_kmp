package pas.tmdb.feature.details.impl.store

import pas.tmdb.feature.details.api.DetailsMediaType

sealed interface DetailsLabel {
    data object NavigateBack : DetailsLabel
    data object NavigateToLogin : DetailsLabel
    data class NavigateToPerson(val personId: Int) : DetailsLabel
    data class NavigateToDetails(val id: Int, val mediaType: DetailsMediaType) : DetailsLabel
    data class ShowError(val message: String) : DetailsLabel
}
