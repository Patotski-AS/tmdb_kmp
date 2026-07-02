package pas.tmdb.data.storage.impl

import org.koin.dsl.module
import pas.tmdb.data.storage.api.SessionStorage

val storageModule = module {
    single<SessionStorage> { createPlatformSessionStorage() }
}
