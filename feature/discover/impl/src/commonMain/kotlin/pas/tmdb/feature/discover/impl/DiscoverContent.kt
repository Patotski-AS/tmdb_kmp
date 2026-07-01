package pas.tmdb.feature.discover.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.feature.discover.api.DiscoverComponent
import pas.tmdb.feature.discover.api.DiscoverIntent
import pas.tmdb.feature.discover.api.MovieItemModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverContent(
    component: DiscoverComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    when {
        model.isLoading && model.movies.isEmpty() -> {
            LoadingState(modifier = modifier)
        }

        model.error != null && model.movies.isEmpty() -> {
            ErrorState(
                message = model.error.orEmpty(),
                onRetry = { component.onIntent(DiscoverIntent.Load) },
                modifier = modifier,
            )
        }

        model.movies.isEmpty() -> {
            EmptyState(modifier = modifier)
        }

        else -> {
            MoviesList(
                movies = model.movies,
                isRefreshing = model.isRefreshing,
                isLoadingMore = model.isLoadingMore,
                endReached = model.endReached,
                onRefresh = { component.onIntent(DiscoverIntent.Refresh) },
                onLoadMore = { component.onIntent(DiscoverIntent.LoadMore) },
                onMovieClick = { movieId ->
                    component.onIntent(DiscoverIntent.OnMovieClick(movieId))
                },
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesList(
    movies: List<MovieItemModel>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    endReached: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, movies.size, endReached, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex to layoutInfo.totalItemsCount
        }.collect { (lastVisibleIndex, totalItems) ->
            if (totalItems > 0 && lastVisibleIndex >= totalItems - 3 && !endReached && !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                )
            }

            if (isLoadingMore) {
                item(key = "loading_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieItemModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(72.dp)
                    .height(108.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                movie.releaseDate?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                    Text(
                        text = releaseDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "★ ${"%.1f".format(movie.voteAverage)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Фильмы не найдены",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
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
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Повторить")
        }
    }
}
