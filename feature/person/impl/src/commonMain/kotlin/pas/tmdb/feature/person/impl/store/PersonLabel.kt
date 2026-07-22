package pas.tmdb.feature.person.impl.store

import pas.tmdb.feature.details.api.DetailsMediaType

sealed interface PersonLabel {
    data object NavigateBack : PersonLabel
    data class NavigateToDetails(
        val id: Int,
        val mediaType: DetailsMediaType,
    ) : PersonLabel
    data class ShowError(val message: String) : PersonLabel
}
