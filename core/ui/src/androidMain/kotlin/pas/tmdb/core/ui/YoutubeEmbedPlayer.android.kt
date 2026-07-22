package pas.tmdb.core.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
actual fun YoutubeEmbedPlayer(
    videoKey: String,
    modifier: Modifier,
    onEmbedNotAllowed: () -> Unit,
) {
    val lifecycleOwner = LocalContext.current.findLifecycleOwner()
    val currentOnEmbedNotAllowed by rememberUpdatedState(onEmbedNotAllowed)

    key(videoKey) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                YouTubePlayerView(context).apply {
                    lifecycleOwner.lifecycle.addObserver(this)
                    addYouTubePlayerListener(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(videoKey, 0f)
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError,
                            ) {
                                if (error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER) {
                                    currentOnEmbedNotAllowed()
                                }
                            }
                        },
                    )
                }
            },
            onRelease = { playerView ->
                playerView.release()
            },
        )
    }
}

private fun Context.findLifecycleOwner(): LifecycleOwner {
    var context = this
    while (context is ContextWrapper) {
        if (context is LifecycleOwner) return context
        context = context.baseContext
    }
    error("LifecycleOwner not found in context chain")
}
