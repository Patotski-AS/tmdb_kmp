package pas.tmdb.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.feature.details.impl.DetailsContent
import pas.tmdb.feature.login.api.LoginChild
import pas.tmdb.feature.login.api.title
import pas.tmdb.feature.login.impl.LoginContent
import pas.tmdb.feature.main.api.MainComponent
import pas.tmdb.feature.media.impl.MediaContent
import pas.tmdb.feature.person.impl.PersonContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier,
) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        modifier = modifier.fillMaxSize(),
        animation = stackAnimation(fade()),
    ) { child ->
        when (val instance = child.instance) {
            is MainComponent.Child.Media -> {
                MediaContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is MainComponent.Child.Details -> {
                DetailsContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is MainComponent.Child.Person -> {
                PersonContent(
                    component = instance.component,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is MainComponent.Child.Profile -> {
                val profileChildStack by instance.component.childStack.subscribeAsState()
                val profileTitle = when (val profileChild = profileChildStack.active.instance) {
                    LoginChild.Menu -> "Профиль"
                    is LoginChild.Section -> profileChild.item.title
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = { Text(profileTitle) },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (!instance.component.onBack()) {
                                            component.onBack()
                                        }
                                    },
                                ) {
                                    Text("←")
                                }
                            },
                        )
                    },
                ) { padding ->
                    LoginContent(
                        component = instance.component,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    )
                }
            }
        }
    }
}
