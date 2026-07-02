package pas.tmdb.feature.login.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberPlatformBrowserLauncher(): (String) -> Unit =
    remember {
        { url ->
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }
