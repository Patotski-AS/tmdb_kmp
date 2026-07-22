package pas.tmdb.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun rememberOpenYoutubeVideoExternally(): (String) -> Unit {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) {
        { videoKey -> uriHandler.openUri(youtubeEmbedUrl(videoKey)) }
    }
}
