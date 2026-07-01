package pas.tmdb.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.main.api.MainComponent

class DefaultRootComponent(
    componentContext: ComponentContext,
    mainComponentFactory: (ComponentContext) -> MainComponent,
) : RootComponent, ComponentContext by componentContext {

    private val _main = MutableValue(mainComponentFactory(this))

    override val main: Value<MainComponent> = _main
}
