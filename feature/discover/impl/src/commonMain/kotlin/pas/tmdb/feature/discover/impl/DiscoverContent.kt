package pas.tmdb.feature.discover.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.core.ui.PlaceholderScreen
import pas.tmdb.feature.discover.api.DiscoverComponent

@Composable
fun DiscoverContent(
    component: DiscoverComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    PlaceholderScreen(
        title = model.title,
        subtitle = "Фаза 3 — popular movies",
        modifier = modifier,
    )
}
