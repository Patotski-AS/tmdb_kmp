package pas.tmdb.app

import android.app.Application
import pas.tmdb.data.storage.impl.initializeAndroidSessionStorage

class TmdbApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidSessionStorage(this)
    }
}
