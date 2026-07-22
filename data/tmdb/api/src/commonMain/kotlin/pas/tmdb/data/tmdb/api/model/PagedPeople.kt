package pas.tmdb.data.tmdb.api.model

data class PagedPeople(
    val people: List<Person>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
)
