package pas.tmdb.data.storage.impl

import pas.tmdb.data.storage.api.SessionStorage

internal actual fun createPlatformSessionStorage(): SessionStorage =
    InMemorySessionStorage()
