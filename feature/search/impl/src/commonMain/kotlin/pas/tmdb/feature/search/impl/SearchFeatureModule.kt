package pas.tmdb.feature.search.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.search.api.SearchComponent
import pas.tmdb.feature.search.impl.store.SearchStoreFactory

val searchFeatureModule = module {
    factory { SearchStoreFactory(storeFactory = get(), movieRepository = get()) }
    factory<SearchComponent> { (componentContext: ComponentContext, onNavigateToDetails: (Int) -> Unit) ->
        DefaultSearchComponent(
            componentContext = componentContext,
            storeFactory = get(),
            onNavigateToDetails = onNavigateToDetails,
        )
    }
}
