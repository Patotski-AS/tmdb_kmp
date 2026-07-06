package pas.tmdb.feature.login.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.auth.api.SessionState
import pas.tmdb.core.ui.MediaPosterGrid
import pas.tmdb.core.ui.MediaPosterItem
import pas.tmdb.core.ui.ProfileAvatar
import pas.tmdb.feature.login.api.FavoriteMovieModel
import pas.tmdb.feature.login.api.LoginChild
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.login.api.LoginIntent
import pas.tmdb.feature.login.api.LoginModel
import pas.tmdb.feature.login.api.ProfileMenuItem
import pas.tmdb.feature.login.api.title

@Composable
fun LoginContent(
    component: LoginComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    val childStack by component.childStack.subscribeAsState()

    PlatformBrowserEffect(component)

    Children(
        stack = childStack,
        modifier = modifier.fillMaxSize(),
        animation = stackAnimation(fade()),
    ) { child ->
        when (val instance = child.instance) {
            LoginChild.Menu -> {
                when (val sessionState = model.sessionState) {
                    is SessionState.Guest -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            GuestContent(
                                isLoading = model.isLoading,
                                isAwaitingApproval = model.isAwaitingApproval,
                                error = model.error,
                                onLoginWithBrowser = { component.onIntent(LoginIntent.Login) },
                                onLoginWithCredentials = { username, password ->
                                    component.onIntent(LoginIntent.LoginWithCredentials(username, password))
                                },
                                onConfirmApproval = { component.onIntent(LoginIntent.ConfirmApproval) },
                                onCancelApproval = { component.onIntent(LoginIntent.CancelApproval) },
                                onDismissError = { component.onIntent(LoginIntent.DismissError) },
                            )
                        }
                    }

                    is SessionState.Authenticated -> {
                        ProfileMenuScreen(
                            username = sessionState.username,
                            accountId = sessionState.accountId,
                            isLoading = model.isLoading,
                            onViewProfile = { component.onIntent(LoginIntent.OnViewProfileClick) },
                            onMenuItemClick = { component.onIntent(LoginIntent.OnMenuItemClick(it)) },
                            onLogout = { component.onIntent(LoginIntent.Logout) },
                        )
                    }
                }
            }

            is LoginChild.Section -> {
                ProfileSectionScreen(
                    item = instance.item,
                    model = model,
                    onIntent = component::onIntent,
                )
            }
        }
    }
}

private val ProfileMenuActivitySection: List<ProfileMenuItem> = listOf(
    ProfileMenuItem.Discussions,
    ProfileMenuItem.Lists,
    ProfileMenuItem.Reviews,
    ProfileMenuItem.Ratings,
    ProfileMenuItem.Watchlist,
    ProfileMenuItem.Favorites,
)

private val ProfileMenuAccountSection: List<ProfileMenuItem> = listOf(
    ProfileMenuItem.ApiSubscription,
    ProfileMenuItem.EditProfile,
    ProfileMenuItem.Settings,
)

@Composable
private fun ProfileMenuScreen(
    username: String?,
    accountId: Int,
    isLoading: Boolean,
    onViewProfile: () -> Unit,
    onMenuItemClick: (ProfileMenuItem) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = username?.takeIf { it.isNotBlank() } ?: "Аккаунт #$accountId"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        ProfileHeader(
            displayName = displayName,
            onViewProfile = onViewProfile,
        )

        HorizontalDivider()

        ProfileMenuSection(
            items = ProfileMenuActivitySection,
            onMenuItemClick = onMenuItemClick,
        )

        HorizontalDivider()

        ProfileMenuSection(
            items = ProfileMenuAccountSection,
            onMenuItemClick = onMenuItemClick,
        )

        HorizontalDivider()

        LogoutMenuRow(
            isLoading = isLoading,
            onLogout = onLogout,
        )
    }
}

@Composable
private fun ProfileSectionScreen(
    item: ProfileMenuItem,
    model: LoginModel,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(item) {
        onIntent(LoginIntent.ScreenOpened(item))
    }

    when (item) {
        ProfileMenuItem.Favorites -> {
            FavoritesSectionContent(
                favorites = model.favorites,
                isLoading = model.isFavoritesLoading,
                error = model.favoritesError,
                onFavoriteClick = { onIntent(LoginIntent.OnFavoriteClick(it)) },
                onRefresh = { onIntent(LoginIntent.RefreshFavorites) },
                modifier = modifier,
            )
        }

        else -> {
            ProfileWebSectionContent(
                item = item,
                onOpenWebsite = { onIntent(LoginIntent.OpenOnWebsite(item)) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FavoritesSectionContent(
    favorites: List<FavoriteMovieModel>,
    isLoading: Boolean,
    error: String?,
    onFavoriteClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading && favorites.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        error != null && favorites.isEmpty() -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text("Повторить")
                }
            }
        }

        favorites.isEmpty() -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Вы пока не добавили фильмы в избранное",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        else -> {
            MediaPosterGrid(
                items = favorites.map { it.toPosterItem() },
                isLoadingMore = false,
                endReached = true,
                onLoadMore = {},
                onItemClick = onFavoriteClick,
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
            )
        }
    }
}

@Composable
private fun ProfileWebSectionContent(
    item: ProfileMenuItem,
    onOpenWebsite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Этот раздел доступен на сайте TMDB",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        )
        Button(onClick = onOpenWebsite) {
            Text("Открыть на themoviedb.org")
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewProfile)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileAvatar(displayName = displayName, size = 64.dp)
        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Смотреть профиль",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileMenuSection(
    items: List<ProfileMenuItem>,
    onMenuItemClick: (ProfileMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            ProfileMenuRow(
                label = item.title,
                onClick = { onMenuItemClick(item) },
            )
        }
    }
}

@Composable
private fun ProfileMenuRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LogoutMenuRow(
    isLoading: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onLogout)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = "Выйти",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun GuestContent(
    isLoading: Boolean,
    isAwaitingApproval: Boolean,
    error: String?,
    onLoginWithBrowser: () -> Unit,
    onLoginWithCredentials: (username: String, password: String) -> Unit,
    onConfirmApproval: () -> Unit,
    onCancelApproval: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "Войдите в аккаунт TMDB, чтобы добавлять фильмы в избранное",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        error?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            OutlinedButton(onClick = onDismissError) {
                Text("Закрыть")
            }
        }

        when {
            isAwaitingApproval -> {
                Text(
                    text = "Подтвердите вход на themoviedb.org, затем нажмите «Продолжить»",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onConfirmApproval,
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                    } else {
                        Text("Продолжить")
                    }
                }
                OutlinedButton(
                    onClick = onCancelApproval,
                    enabled = !isLoading,
                ) {
                    Text("Отмена")
                }
            }

            isLoading -> {
                CircularProgressIndicator()
            }

            else -> {
                Button(
                    onClick = onLoginWithBrowser,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Войти через сайт TMDB")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "или логин и пароль",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Логин TMDB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        onLoginWithCredentials(username.trim(), password)
                        password = ""
                    },
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Войти")
                }
            }
        }
    }
}

private fun FavoriteMovieModel.toPosterItem(): MediaPosterItem =
    MediaPosterItem(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage.takeIf { it > 0.0 },
        isFavorite = true,
    )
