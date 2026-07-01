package pas.tmdb.data.config.impl

import pas.tmdb.data.config.api.TmdbConfig
import pas.tmdb.data.config.impl.internal.TMDB_API_KEY

class TmdbConfigImpl : TmdbConfig {
    override val apiKey: String = TMDB_API_KEY
    override val baseUrl: String = "https://api.themoviedb.org/3/"
}
