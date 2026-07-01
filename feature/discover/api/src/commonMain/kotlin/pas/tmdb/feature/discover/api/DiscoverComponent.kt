package pas.tmdb.feature.discover.api

import com.arkivanov.decompose.value.Value

interface DiscoverComponent {
    val model: Value<DiscoverModel>
    fun onIntent(intent: DiscoverIntent)
}

data class DiscoverModel(
    val title: String = "Discover",
)

sealed interface DiscoverIntent {
    data object Load : DiscoverIntent
}
