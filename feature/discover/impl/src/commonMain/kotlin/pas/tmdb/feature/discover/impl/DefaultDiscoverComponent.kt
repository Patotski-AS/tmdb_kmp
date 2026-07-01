package pas.tmdb.feature.discover.impl

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.discover.api.DiscoverIntent
import pas.tmdb.feature.discover.api.DiscoverModel

class DefaultDiscoverComponent(
    componentContext: ComponentContext,
) : DiscoverComponent, ComponentContext by componentContext {

    private val _model = MutableValue(DiscoverModel())

    override val model: Value<DiscoverModel> = _model

    override fun onIntent(intent: DiscoverIntent) = Unit
}
