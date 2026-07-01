package pas.tmdb.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.feature.details.impl.DetailsContent
import pas.tmdb.feature.discover.impl.DiscoverContent
import pas.tmdb.feature.main.api.MainComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier,
) {
    val childStack by component.childStack.subscribeAsState()
    val selectedTab by component.selectedTab.subscribeAsState()
    val activeChild = childStack.active.instance

    when (activeChild) {
        is MainComponent.Child.Details -> {
            DetailsContent(
                component = activeChild.component,
                modifier = modifier.fillMaxSize(),
            )
        }

        is MainComponent.Child.Discover -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(title = { Text("TMDB KMP") })
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == MainComponent.Tab.Discover,
                            onClick = { component.onTabSelected(MainComponent.Tab.Discover) },
                            icon = { Text("★") },
                            label = { Text("Главная") },
                        )
                    }
                },
            ) { padding ->
                Children(
                    stack = childStack,
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) { child ->
                    when (val instance = child.instance) {
                        is MainComponent.Child.Discover -> DiscoverContent(component = instance.component)
                        is MainComponent.Child.Details -> Unit
                    }
                }
            }
        }
    }
}
