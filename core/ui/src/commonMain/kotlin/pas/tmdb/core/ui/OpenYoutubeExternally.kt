package pas.tmdb.core.ui

import androidx.compose.runtime.Composable

/** Same destination as themoviedb.org — opens in YouTube app / browser. */
fun youtubeWatchUrl(videoKey: String): String =
    "https://www.youtube.com/watch?v=$videoKey"

@Composable
expect fun rememberOpenYoutubeVideoExternally(): (String) -> Unit
