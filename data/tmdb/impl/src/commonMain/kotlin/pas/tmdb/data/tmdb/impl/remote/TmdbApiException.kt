package pas.tmdb.data.tmdb.impl.remote

internal class TmdbApiException(
    message: String,
    val httpStatusCode: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    val isUnauthorized: Boolean get() = httpStatusCode == 401
}
