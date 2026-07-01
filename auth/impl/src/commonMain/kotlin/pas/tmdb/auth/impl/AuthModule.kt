package pas.tmdb.auth.impl

import org.koin.dsl.module
import pas.tmdb.auth.api.AuthResultHandler
import pas.tmdb.auth.api.SessionRepository

val authModule = module {
    single { SessionManager() }
    single<SessionRepository> { get<SessionManager>() }
    single<AuthResultHandler> { get<SessionManager>() }
}
