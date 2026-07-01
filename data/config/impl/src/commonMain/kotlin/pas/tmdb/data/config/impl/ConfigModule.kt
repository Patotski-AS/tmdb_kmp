package pas.tmdb.data.config.impl

import org.koin.dsl.module
import pas.tmdb.data.config.api.TmdbConfig

val configModule = module {
    single<TmdbConfig> { TmdbConfigImpl() }
}
