package pas.tmdb.core.network

import pas.tmdb.core.network.BuildConfig

actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG
