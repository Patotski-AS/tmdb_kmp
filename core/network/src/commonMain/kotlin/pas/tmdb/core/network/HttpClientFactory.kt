package pas.tmdb.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient = createPlatformHttpClient {
    install(ContentNegotiation) {
        json(createJson())
    }
    if (isDebugBuild()) {
        install(Logging) {
            logger = TmdbHttpLogger
            level = LogLevel.ALL
        }
    }
}

private fun createJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

expect fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
