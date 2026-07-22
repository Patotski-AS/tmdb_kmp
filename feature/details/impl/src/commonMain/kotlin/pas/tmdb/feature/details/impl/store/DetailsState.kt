package pas.tmdb.feature.details.impl.store

import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.details.api.DetailsImageGallery
import pas.tmdb.feature.details.api.DetailsMediaType
import pas.tmdb.feature.details.api.DetailsModel

data class DetailsState(
    val mediaId: Int,
    val mediaType: DetailsMediaType,
    val details: DetailsModel = DetailsModel(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionState: SessionState = SessionState.Guest,
    val isFavorite: Boolean? = null,
    val isFavoriteLoading: Boolean = false,
    val imageGallery: DetailsImageGallery? = null,
)
