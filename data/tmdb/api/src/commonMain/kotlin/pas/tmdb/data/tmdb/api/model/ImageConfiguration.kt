package pas.tmdb.data.tmdb.api.model

data class ImageConfiguration(
    val posterBaseUrl: String,
    val posterSize: String = DEFAULT_POSTER_SIZE,
) {
    companion object {
        const val DEFAULT_POSTER_SIZE: String = "w500"
    }
}
