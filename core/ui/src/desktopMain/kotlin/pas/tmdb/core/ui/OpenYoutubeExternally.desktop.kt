package pas.tmdb.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberOpenYoutubeVideoExternally(): (String) -> Unit =
    remember {
        { videoKey ->
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(youtubeWatchUrl(videoKey)))
            }
        }
    }
