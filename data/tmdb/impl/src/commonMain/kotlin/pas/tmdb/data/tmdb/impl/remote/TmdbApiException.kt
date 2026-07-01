package pas.tmdb.data.tmdb.impl.remote

internal class TmdbApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
