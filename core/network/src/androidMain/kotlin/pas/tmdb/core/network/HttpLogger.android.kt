package pas.tmdb.core.network

import android.util.Log

internal actual fun logHttpMessage(message: String) {
    Log.d(TAG, message)
}

private const val TAG: String = "TmdbHttp"
