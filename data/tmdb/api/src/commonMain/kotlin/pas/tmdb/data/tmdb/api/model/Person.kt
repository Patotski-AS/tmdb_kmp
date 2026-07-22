package pas.tmdb.data.tmdb.api.model

data class Person(
    val id: Int,
    val name: String,
    val profileUrl: String?,
    val knownForDepartment: String?,
)
