package pas.tmdb.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import pas.tmdb.auth.impl.authModule
import pas.tmdb.core.navigation.DefaultRootComponent
import pas.tmdb.core.navigation.RootComponent
import pas.tmdb.core.ui.TmdbTheme
import pas.tmdb.data.config.impl.configModule
import pas.tmdb.data.storage.impl.storageModule
import pas.tmdb.data.tmdb.impl.tmdbDataModule
import pas.tmdb.feature.details.impl.detailsFeatureModule
import pas.tmdb.feature.discover.impl.discoverFeatureModule
import pas.tmdb.feature.login.impl.loginFeatureModule
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.main.impl.mainFeatureModule
import pas.tmdb.feature.search.impl.searchFeatureModule
import org.koin.compose.KoinApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun App() {
    KoinApplication(application = {
        modules(
            configModule,
            storageModule,
            authModule,
            tmdbDataModule,
            discoverFeatureModule,
            detailsFeatureModule,
            searchFeatureModule,
            loginFeatureModule,
            mainFeatureModule,
            appModule,
        )
    }) {
        TmdbTheme {
            val root = createRootComponent()
            RootContent(root)
        }
    }
}

@Composable
private fun RootContent(component: RootComponent) {
    val main by component.main.subscribeAsState()
    MainContent(component = main)
}

@Composable
private fun createRootComponent(): RootComponent {
    val koin = getKoin()
    val lifecycle = LifecycleRegistry()
    val componentContext = DefaultComponentContext(lifecycle = lifecycle)

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
