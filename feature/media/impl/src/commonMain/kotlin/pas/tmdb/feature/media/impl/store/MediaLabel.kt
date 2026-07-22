package pas.tmdb.feature.media.impl.store

import pas.tmdb.feature.media.api.ContentType

sealed interface MediaLabel {
    data class NavigateToDetails(
        val id: Int,
        val contentType: ContentType,
    ) : MediaLabel
    data object NavigateToProfile : MediaLabel
    data class ShowError(val message: String) : MediaLabel
}
