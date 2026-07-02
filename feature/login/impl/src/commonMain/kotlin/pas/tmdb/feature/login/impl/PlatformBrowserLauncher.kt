package pas.tmdb.feature.login.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import pas.tmdb.feature.login.api.LoginComponent

@Composable
expect fun rememberPlatformBrowserLauncher(): (String) -> Unit

@Composable
fun PlatformBrowserEffect(component: LoginComponent) {
    val openUrl = rememberPlatformBrowserLauncher()
    LaunchedEffect(component, openUrl) {
        component.browserUrlEvents.collect { url ->
            openUrl(url)
        }
    }
}
