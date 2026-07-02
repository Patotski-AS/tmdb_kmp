package pas.tmdb.feature.media.api

data class MediaFilters(
    val genreId: Int? = null,
    val sortBy: String? = null,
) {
    val isActive: Boolean
        get() = genreId != null || sortBy != null
}
