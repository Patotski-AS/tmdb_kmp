package pas.tmdb.feature.details.impl.store

import pas.tmdb.feature.details.api.DetailsModel

data class DetailsState(
    val movieId: Int,
    val details: DetailsModel = DetailsModel(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
