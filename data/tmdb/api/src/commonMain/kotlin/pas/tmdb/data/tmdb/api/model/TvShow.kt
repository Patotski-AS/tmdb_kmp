package pas.tmdb.data.tmdb.api.model

data class TvShow(
    val id: Int,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val voteAverage: Double,
    val firstAirDate: String?,
)
