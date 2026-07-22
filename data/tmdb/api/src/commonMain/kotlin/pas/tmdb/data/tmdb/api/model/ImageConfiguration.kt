package pas.tmdb.data.tmdb.api.model

data class ImageConfiguration(
    val posterBaseUrl: String,
    val posterSize: String = DEFAULT_POSTER_SIZE,
    val backdropSize: String = DEFAULT_BACKDROP_SIZE,
    val profileSize: String = DEFAULT_PROFILE_SIZE,
) {
    companion object {
        const val DEFAULT_POSTER_SIZE: String = "w500"
        const val DEFAULT_BACKDROP_SIZE: String = "w780"
        const val DEFAULT_PROFILE_SIZE: String = "w185"
    }
}
