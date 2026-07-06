package pas.tmdb.feature.media.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.dsl.module
import pas.tmdb.feature.media.api.MediaComponent
import pas.tmdb.feature.media.impl.store.MediaStoreFactory

val mediaFeatureModule = module {
    single<StoreFactory> { DefaultStoreFactory() }
    factory {
        MediaStoreFactory(
            storeFactory = get(),
            movieRepository = get(),
            favoriteMoviesCache = get(),
            sessionRepository = get(),
        )
    }
    factory<MediaComponent> {
        (
            componentContext: ComponentContext,
            onNavigateToDetails: (Int) -> Unit,
            onNavigateToProfile: () -> Unit,
        ) ->
        DefaultMediaComponent(
            componentContext = componentContext,
            storeFactory = get(),
            onNavigateToDetails = onNavigateToDetails,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}
