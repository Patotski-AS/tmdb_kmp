package pas.tmdb.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import pas.tmdb.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TMDB",
    ) {
        App()
    }
}
