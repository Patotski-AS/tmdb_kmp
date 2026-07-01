package pas.tmdb.data.tmdb.api.model

data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val voteAverage: Double,
    val releaseDate: String?,
    val runtime: Int?,
    val genres: List<String>,
)
