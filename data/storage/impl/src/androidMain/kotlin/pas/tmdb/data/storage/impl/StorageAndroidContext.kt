package pas.tmdb.data.storage.impl

import android.content.Context

object StorageAndroidContext {
    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun requireContext(): Context =
        applicationContext ?: error("StorageAndroidContext is not initialized")
}

fun initializeAndroidSessionStorage(context: Context) {
    StorageAndroidContext.init(context)
}
