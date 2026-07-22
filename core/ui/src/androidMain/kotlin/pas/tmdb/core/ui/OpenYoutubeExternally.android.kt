package pas.tmdb.core.ui

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOpenYoutubeVideoExternally(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { videoKey ->
            val youtubeAppUri = Uri.parse("vnd.youtube:$videoKey")
            val appIntent = Intent(Intent.ACTION_VIEW, youtubeAppUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
            } else {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(context, Uri.parse(youtubeEmbedUrl(videoKey)))
            }
        }
    }
}
