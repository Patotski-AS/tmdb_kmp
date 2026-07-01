package pas.tmdb.feature.discover.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.discover.impl.store.DiscoverStoreFactory

val discoverFeatureModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    factory { DiscoverStoreFactory(storeFactory = get(), movieRepository = get(), sessionRepository = get()) }
    factory<DiscoverComponent> { (componentContext: ComponentContext, onNavigateToDetails: (Int) -> Unit) ->
        DefaultDiscoverComponent(
            componentContext = componentContext,
            storeFactory = get(),
            onNavigateToDetails = onNavigateToDetails,
        )
    }
}
