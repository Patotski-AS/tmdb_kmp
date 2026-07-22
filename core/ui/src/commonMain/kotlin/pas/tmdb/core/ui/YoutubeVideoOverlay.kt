package pas.tmdb.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex

internal fun youtubeEmbedHtml(videoKey: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            html, body { margin: 0; height: 100%; background-color: #000; }
            #player { width: 100%; height: 100%; }
        </style>
    </head>
    <body>
        <div id="player"></div>
        <script src="https://www.youtube.com/iframe_api"></script>
        <script>
            function onYouTubeIframeAPIReady() {
                new YT.Player('player', {
                    width: '100%',
                    height: '100%',
                    videoId: '$videoKey',
                    playerVars: { autoplay: 1, playsinline: 1, rel: 0, fs: 1 },
                    events: {
                        onError: function (event) {
                            if (window.webkit && window.webkit.messageHandlers &&
                                window.webkit.messageHandlers.onEmbedError) {
                                window.webkit.messageHandlers.onEmbedError.postMessage(event.data);
                            }
                        }
                    }
                });
            }
        </script>
    </body>
    </html>
""".trimIndent()

internal fun youtubeEmbedUrl(videoKey: String): String =
    "https://www.youtube.com/embed/$videoKey?autoplay=1&playsinline=1&rel=0&modestbranding=1&fs=1&enablejsapi=1"

@Composable
fun YoutubeVideoOverlay(
    videoKey: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openExternally = rememberOpenYoutubeVideoExternally()
    val scrimInteractionSource = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable(
                        interactionSource = scrimInteractionSource,
                        indication = null,
                        onClick = onDismiss,
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .zIndex(1f),
            ) {
                YoutubeEmbedPlayer(
                    videoKey = videoKey,
                    modifier = Modifier.fillMaxSize(),
                    onEmbedNotAllowed = { openExternally(videoKey) },
                )
            }

            TextButton(
                onClick = { openExternally(videoKey) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .zIndex(2f),
            ) {
                Text(
                    text = "Открыть в YouTube",
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(48.dp)
                    .zIndex(3f),
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

@Composable
expect fun YoutubeEmbedPlayer(
    videoKey: String,
    modifier: Modifier = Modifier,
    onEmbedNotAllowed: () -> Unit = {},
)
