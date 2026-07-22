package pas.tmdb.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

@Composable
actual fun YoutubeEmbedPlayer(
    videoKey: String,
    modifier: Modifier,
    onEmbedNotAllowed: () -> Unit,
) {
    val embedUrl = youtubeEmbedUrl(videoKey)

    LaunchedEffect(videoKey) {
        if (Desktop.isDesktopSupported()) {
            runCatching {
                Desktop.getDesktop().browse(URI(embedUrl))
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "На Desktop видео открывается в браузере",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
        TextButton(
            onClick = {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI(embedUrl))
                }
            },
        ) {
            Text(
                text = "Открыть снова",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
