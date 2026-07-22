package pas.tmdb.feature.details.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.details.api.DetailsComponent
import pas.tmdb.feature.details.impl.store.DetailsStoreFactory

val detailsFeatureModule = module {
    factory {
        DetailsStoreFactory(
            storeFactory = get(),
            movieRepository = get(),
            accountRepository = get(),
            sessionRepository = get(),
        )
    }
    factory<DetailsComponent> { (
        componentContext: ComponentContext,
        movieId: Int,
        onBack: () -> Unit,
        onNavigateToLogin: () -> Unit,
        onNavigateToDetails: (Int) -> Unit,
    ) ->
        DefaultDetailsComponent(
            componentContext = componentContext,
            movieId = movieId,
            storeFactory = get(),
            onBack = onBack,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToDetails = onNavigateToDetails,
        )
    }
}
