package pas.tmdb.data.tmdb.impl

import org.koin.dsl.module
import pas.tmdb.data.tmdb.api.AccountRepository
import pas.tmdb.data.tmdb.api.FavoriteMoviesCache
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.impl.cache.FavoriteMoviesCacheImpl
import pas.tmdb.data.tmdb.impl.cache.FavoriteMoviesSynchronizer
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.repository.AccountRepositoryImpl
import pas.tmdb.data.tmdb.impl.repository.MovieRepositoryImpl

val tmdbDataModule = module {
    single { TmdbApiClient(httpClient = get(), config = get()) }
    single<MovieRepository> { MovieRepositoryImpl(apiClient = get()) }
    single<FavoriteMoviesCache> { FavoriteMoviesCacheImpl() }
    single(createdAtStart = true) {
        FavoriteMoviesSynchronizer(
            sessionRepository = get(),
            accountRepository = get(),
            favoriteMoviesCache = get(),
        )
    }
    single<AccountRepository> {
        AccountRepositoryImpl(
            apiClient = get(),
            sessionRepository = get(),
            favoriteMoviesCache = get(),
            movieRepository = get(),
        )
    }
}
