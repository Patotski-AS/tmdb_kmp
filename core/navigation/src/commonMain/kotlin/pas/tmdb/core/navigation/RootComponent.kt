package pas.tmdb.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.main.api.MainComponent

interface RootComponent {
    val main: Value<MainComponent>

    fun interface Factory {
        fun create(componentContext: ComponentContext): RootComponent
    }
}
