package pas.tmdb.data.tmdb.api.model

data class PagedMovies(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
)
