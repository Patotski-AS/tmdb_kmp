package pas.tmdb.core.network

actual fun isDebugBuild(): Boolean = !System.getProperty("tmdb.release", "false").toBoolean()
