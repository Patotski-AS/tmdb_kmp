package pas.tmdb.data.config.impl

import pas.tmdb.data.config.api.TmdbConfig
import pas.tmdb.data.config.impl.internal.TMDB_ACCESS_TOKEN

class TmdbConfigImpl : TmdbConfig {
    override val accessToken: String = TMDB_ACCESS_TOKEN
    override val baseUrl: String = "https://api.themoviedb.org/3/"
}
