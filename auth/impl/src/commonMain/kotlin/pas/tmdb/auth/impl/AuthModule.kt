package pas.tmdb.auth.impl

import org.koin.dsl.module
import pas.tmdb.auth.api.AuthResultHandler
import pas.tmdb.auth.api.SessionRepository
import pas.tmdb.auth.api.TmdbSessionClient
import pas.tmdb.auth.impl.remote.TmdbSessionClientImpl

val authModule = module {
    single { TmdbSessionClientImpl(httpClient = get(), config = get()) }
    single<TmdbSessionClient> { get<TmdbSessionClientImpl>() }
    single { SessionManager(sessionStorage = get()) }
    single<SessionRepository> { get<SessionManager>() }
    single<AuthResultHandler> { get<SessionManager>() }
}
