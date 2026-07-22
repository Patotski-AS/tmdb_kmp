package pas.tmdb.feature.media.api

enum class MediaSortOption(
    val label: String,
) {
    Popularity(label = "По популярности"),
    ReleaseDate(label = "По дате выхода"),
    VoteAverage(label = "По оценке"),
    ;

    fun sortBy(contentType: ContentType): String =
        when (this) {
            Popularity -> "popularity.desc"
            ReleaseDate -> when (contentType) {
                ContentType.Movies -> "primary_release_date.desc"
                ContentType.Tv -> "first_air_date.desc"
                ContentType.People -> "popularity.desc"
            }
            VoteAverage -> "vote_average.desc"
        }

    companion object {
        fun fromSortBy(sortBy: String?): MediaSortOption? =
            when (sortBy) {
                "popularity.desc" -> Popularity
                "primary_release_date.desc",
                "first_air_date.desc",
                -> ReleaseDate
                "vote_average.desc" -> VoteAverage
                else -> null
            }
    }
}
