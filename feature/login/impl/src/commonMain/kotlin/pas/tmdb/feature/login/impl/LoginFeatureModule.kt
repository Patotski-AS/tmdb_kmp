package pas.tmdb.feature.login.impl

import com.arkivanov.decompose.ComponentContext
import org.koin.dsl.module
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.login.impl.store.LoginStoreFactory

val loginFeatureModule = module {
    factory {
        LoginStoreFactory(
            storeFactory = get(),
            sessionClient = get(),
            sessionRepository = get(),
            authResultHandler = get(),
            accountRepository = get(),
        )
    }
    factory<LoginComponent> { (componentContext: ComponentContext, onNavigateToDetails: (Int) -> Unit) ->
        DefaultLoginComponent(
            componentContext = componentContext,
            storeFactory = get(),
            onNavigateToDetails = onNavigateToDetails,
        )
    }
}
