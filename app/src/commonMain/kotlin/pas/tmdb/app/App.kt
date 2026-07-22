package pas.tmdb.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.auth.impl.authModule
import pas.tmdb.core.navigation.DefaultRootComponent
import pas.tmdb.core.navigation.RootComponent
import pas.tmdb.core.ui.TmdbTheme
import pas.tmdb.data.config.impl.configModule
import pas.tmdb.data.storage.impl.storageModule
import pas.tmdb.data.tmdb.impl.tmdbDataModule
import pas.tmdb.feature.details.impl.detailsFeatureModule
import pas.tmdb.feature.media.impl.mediaFeatureModule
import pas.tmdb.feature.login.impl.loginFeatureModule
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.main.impl.mainFeatureModule
import pas.tmdb.feature.person.impl.personFeatureModule
import pas.tmdb.feature.search.impl.searchFeatureModule
import org.koin.compose.KoinApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import io.ktor.client.HttpClient

@Composable
fun App() {
    KoinApplication(application = {
        modules(
            configModule,
            storageModule,
            authModule,
            tmdbDataModule,
            mediaFeatureModule,
            detailsFeatureModule,
            searchFeatureModule,
            loginFeatureModule,
            personFeatureModule,
            mainFeatureModule,
            appModule,
        )
    }) {
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory(httpClient = getKoin().get<HttpClient>()))
                }
                .crossfade(true)
                .build()
        }
        TmdbTheme {
            val componentContext = rememberRootComponentContext()
            val root = remember(componentContext) { createRootComponent(componentContext) }
            RootContent(root)
        }
    }
}

@Composable
private fun RootContent(component: RootComponent) {
    val main by component.main.subscribeAsState()
    MainContent(component = main)
}

private fun createRootComponent(componentContext: ComponentContext): RootComponent {
    val koin = getKoin()

    return DefaultRootComponent(
        componentContext = componentContext,
        mainComponentFactory = { childContext ->
            koin.get<MainComponent> { parametersOf(childContext) }
        },
    )
}

private val appModule = module {
    // Reserved for RootComponent factory wiring if moved from composable.
}
