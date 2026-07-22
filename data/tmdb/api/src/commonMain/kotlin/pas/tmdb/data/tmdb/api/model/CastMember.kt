package pas.tmdb.data.tmdb.api.model

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
    val order: Int,
)
