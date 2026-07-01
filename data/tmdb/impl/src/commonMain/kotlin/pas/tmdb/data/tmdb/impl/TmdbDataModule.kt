package pas.tmdb.data.tmdb.impl

import org.koin.dsl.module
import pas.tmdb.data.tmdb.api.MovieRepository
import pas.tmdb.data.tmdb.impl.remote.TmdbApiClient
import pas.tmdb.data.tmdb.impl.repository.MovieRepositoryImpl

val tmdbDataModule = module {
    single { TmdbApiClient(httpClient = get(), config = get()) }
    single<MovieRepository> { MovieRepositoryImpl(apiClient = get()) }
}
