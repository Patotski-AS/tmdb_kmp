package pas.tmdb.feature.media.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.core.ui.MediaGridLoadingState
import pas.tmdb.core.ui.MediaPosterGrid
import pas.tmdb.core.ui.MediaPosterItem
import pas.tmdb.core.ui.ProfileAvatar
import pas.tmdb.core.ui.TmdbErrorState
import pas.tmdb.core.ui.TmdbMessageState
import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaComponent
import pas.tmdb.feature.media.api.MediaIntent
import pas.tmdb.feature.media.api.MediaItemModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaContent(
    component: MediaComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (model.contentType == ContentType.Movies) {
                FloatingActionButton(
                    onClick = { showFilterSheet = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    FilterFabIcon(modifier = Modifier.size(24.dp))
                }
            }
        },
        topBar = {
            Surface(
                modifier = Modifier.statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MediaSearchField(
                        query = model.searchQuery,
                        contentType = model.contentType,
                        onQueryChange = { component.onIntent(MediaIntent.QueryChanged(it)) },
                        onClear = { component.onIntent(MediaIntent.ClearQuery) },
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { component.onIntent(MediaIntent.OnProfileClick) },
                        contentAlignment = Alignment.Center,
                    ) {
                        ProfileAvatar(
                            displayName = profileDisplayName(model.sessionState),
                            size = 36.dp,
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                NavigationBarItem(
                    selected = model.contentType == ContentType.Movies,
                    onClick = {
                        component.onIntent(MediaIntent.OnContentTypeSelected(ContentType.Movies))
                    },
                    icon = { Text("🎬") },
                    label = { Text("Фильмы") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                NavigationBarItem(
                    selected = model.contentType == ContentType.Tv,
                    onClick = {
                        component.onIntent(MediaIntent.OnContentTypeSelected(ContentType.Tv))
                    },
                    icon = { Text("📺") },
                    label = { Text("Сериалы") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                NavigationBarItem(
                    selected = model.contentType == ContentType.People,
                    onClick = {
                        component.onIntent(MediaIntent.OnContentTypeSelected(ContentType.People))
                    },
                    icon = { Text("🎭") },
                    label = { Text("Актёры") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        },
    ) { padding ->
        MediaBody(
            contentType = model.contentType,
            searchQuery = model.searchQuery,
            items = model.items,
            isLoading = model.isLoading,
            isLoadingMore = model.isLoadingMore,
            error = model.error,
            endReached = model.endReached,
            favoriteIds = model.favoriteIds,
            onLoad = { component.onIntent(MediaIntent.Load) },
            onLoadMore = { component.onIntent(MediaIntent.LoadMore) },
            onItemClick = { id -> component.onIntent(MediaIntent.OnItemClick(id)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }

    if (showFilterSheet) {
        MediaFilterSheet(
            genres = model.genres,
            currentFilters = model.filters,
            onApply = { filters -> component.onIntent(MediaIntent.ApplyFilters(filters)) },
            onReset = { component.onIntent(MediaIntent.ResetFilters) },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaSearchField(
    query: String,
    contentType: ContentType,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = {
            Text(
                text = when (contentType) {
                    ContentType.Movies -> "Поиск фильмов"
                    ContentType.Tv -> "Поиск сериалов"
                    ContentType.People -> "Поиск"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Text("✕")
                }
            }
        },
    )
}

@Composable
private fun MediaBody(
    contentType: ContentType,
    searchQuery: String,
    items: List<MediaItemModel>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    endReached: Boolean,
    favoriteIds: Set<Int>,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (contentType) {
        ContentType.Movies,
        ContentType.Tv,
        -> CatalogContent(
            searchQuery = searchQuery,
            emptyMessage = if (contentType == ContentType.Tv) {
                "Сериалы не найдены"
            } else {
                "Фильмы не найдены"
            },
            items = items,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            error = error,
            endReached = endReached,
            favoriteIds = if (contentType == ContentType.Movies) favoriteIds else emptySet(),
            onLoad = onLoad,
            onLoadMore = onLoadMore,
            onItemClick = onItemClick,
            modifier = modifier,
        )

        ContentType.People -> {
            TmdbMessageState(
                message = "Скоро",
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CatalogContent(
    searchQuery: String,
    emptyMessage: String,
    items: List<MediaItemModel>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    endReached: Boolean,
    favoriteIds: Set<Int>,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSearching = searchQuery.isNotBlank()

    when {
        isLoading && items.isEmpty() -> {
            MediaGridLoadingState(modifier = modifier)
        }

        error != null && items.isEmpty() -> {
            TmdbErrorState(
                message = error,
                onRetry = onLoad,
                modifier = modifier,
            )
        }

        items.isEmpty() && isSearching -> {
            TmdbMessageState(
                message = "Ничего не найдено",
                modifier = modifier,
            )
        }

        items.isEmpty() -> {
            TmdbMessageState(
                message = emptyMessage,
                modifier = modifier,
            )
        }

        else -> {
            MediaPosterGrid(
                items = items.map { it.toPosterItem(favoriteIds) },
                isLoadingMore = isLoadingMore,
                endReached = endReached,
                onLoadMore = onLoadMore,
                onItemClick = onItemClick,
                modifier = modifier,
            )
        }
    }
}

private fun MediaItemModel.toPosterItem(favoriteIds: Set<Int>): MediaPosterItem =
    MediaPosterItem(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
        isFavorite = id in favoriteIds,
    )

private fun profileDisplayName(sessionState: SessionState): String? =
    when (sessionState) {
        is SessionState.Authenticated -> sessionState.username
        SessionState.Guest -> null
    }
