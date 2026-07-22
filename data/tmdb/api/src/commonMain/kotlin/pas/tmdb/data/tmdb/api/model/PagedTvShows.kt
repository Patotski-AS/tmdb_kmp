package pas.tmdb.data.tmdb.api.model

data class PagedTvShows(
    val shows: List<TvShow>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
)
