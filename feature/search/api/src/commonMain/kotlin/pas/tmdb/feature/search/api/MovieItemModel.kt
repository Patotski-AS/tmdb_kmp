package pas.tmdb.feature.search.api

data class MovieItemModel(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val voteAverage: Double,
    val releaseDate: String?,
)
