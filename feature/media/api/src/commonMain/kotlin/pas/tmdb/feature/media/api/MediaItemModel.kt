package pas.tmdb.feature.media.api

data class MediaItemModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double,
)
