package pas.tmdb.feature.search.impl

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import pas.tmdb.core.ui.ShimmerMovieList
import pas.tmdb.core.ui.TmdbErrorState
import pas.tmdb.core.ui.TmdbMessageState
import pas.tmdb.feature.search.api.MovieItemModel
import pas.tmdb.feature.search.api.SearchComponent
import pas.tmdb.feature.search.api.SearchIntent

@Composable
fun SearchContent(
    component: SearchComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchField(
            query = model.query,
            onQueryChange = { component.onIntent(SearchIntent.QueryChanged(it)) },
            onClear = { component.onIntent(SearchIntent.Clear) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                model.isLoading && model.movies.isEmpty() -> {
                    ShimmerMovieList()
                }

                model.error != null && model.movies.isEmpty() -> {
                    TmdbErrorState(
                        message = model.error.orEmpty(),
                        onRetry = { component.onIntent(SearchIntent.QueryChanged(model.query)) },
                    )
                }

                !model.hasSearched && model.movies.isEmpty() -> {
                    TmdbMessageState(message = "Введите название фильма")
                }

                model.hasSearched && model.movies.isEmpty() && !model.isLoading -> {
                    TmdbMessageState(message = "Ничего не найдено")
                }

                else -> {
                    ResultsList(
                        movies = model.movies,
                        isLoadingMore = model.isLoadingMore,
                        endReached = model.endReached,
                        onLoadMore = { component.onIntent(SearchIntent.LoadMore) },
                        onMovieClick = { movieId ->
                            component.onIntent(SearchIntent.OnMovieClick(movieId))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("Поиск фильмов") },
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
private fun ResultsList(
    movies: List<MovieItemModel>,
    isLoadingMore: Boolean,
    endReached: Boolean,
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

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
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

