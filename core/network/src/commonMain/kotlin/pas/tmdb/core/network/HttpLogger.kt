package pas.tmdb.core.network

import io.ktor.client.plugins.logging.Logger

internal object TmdbHttpLogger : Logger {
    override fun log(message: String) {
        logHttpMessage(message)
    }
}

internal expect fun logHttpMessage(message: String)
