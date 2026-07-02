package pas.tmdb.data.config.impl

import io.ktor.client.HttpClient
import org.koin.dsl.module
import pas.tmdb.core.network.createHttpClient
import pas.tmdb.data.config.api.TmdbConfig

val configModule = module {
    single<TmdbConfig> { TmdbConfigImpl() }
    single<HttpClient> {
        val config = get<TmdbConfig>()
        createHttpClient(config.accessToken)
    }
}
