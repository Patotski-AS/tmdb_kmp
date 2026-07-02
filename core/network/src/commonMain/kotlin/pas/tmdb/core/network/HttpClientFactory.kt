package pas.tmdb.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(accessToken: String = ""): HttpClient = createPlatformHttpClient {
    if (accessToken.isNotBlank() && accessToken != PLACEHOLDER_ACCESS_TOKEN) {
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }
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

private const val PLACEHOLDER_ACCESS_TOKEN: String = "your_access_token_here"

expect fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
