package pas.tmdb.data.tmdb.api.model

enum class PersonCreditMediaType {
    Movie,
    Tv,
}

data class PersonCredit(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val mediaType: PersonCreditMediaType,
    val characterOrJob: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val isCast: Boolean,
)
