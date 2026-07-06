package pas.tmdb.feature.media.api

enum class MediaSortOption(
    val sortBy: String,
    val label: String,
) {
    Popularity(sortBy = "popularity.desc", label = "По популярности"),
    ReleaseDate(sortBy = "primary_release_date.desc", label = "По дате выхода"),
    VoteAverage(sortBy = "vote_average.desc", label = "По оценке"),
    ;

    companion object {
        fun fromSortBy(sortBy: String?): MediaSortOption? =
            entries.firstOrNull { it.sortBy == sortBy }
    }
}
