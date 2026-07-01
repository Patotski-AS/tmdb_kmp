package pas.tmdb.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClient(): HttpClient = HttpClient(CIO)
