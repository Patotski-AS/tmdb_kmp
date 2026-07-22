package pas.tmdb.feature.media.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pas.tmdb.feature.media.api.ContentType
import pas.tmdb.feature.media.api.MediaFilters
import pas.tmdb.feature.media.api.MediaGenre
import pas.tmdb.feature.media.api.MediaSortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaFilterSheet(
    contentType: ContentType,
    genres: List<MediaGenre>,
    currentFilters: MediaFilters,
    onApply: (MediaFilters) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftGenreId by remember(currentFilters) { mutableStateOf(currentFilters.genreId) }
    var draftSortBy by remember(currentFilters) { mutableStateOf(currentFilters.sortBy) }
    var genreMenuExpanded by remember { mutableStateOf(false) }

    val selectedGenreLabel = genres
        .firstOrNull { it.id == draftGenreId }
        ?.name
        ?: "Все жанры"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Фильтры",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            GenreDropdown(
                selectedLabel = selectedGenreLabel,
                genres = genres,
                expanded = genreMenuExpanded,
                onExpandedChange = { genreMenuExpanded = it },
                onGenreSelected = { draftGenreId = it },
            )

            SortSection(
                contentType = contentType,
                draftSortBy = draftSortBy,
                onSortByChange = { draftSortBy = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        onReset()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Сбросить")
                }
                Button(
                    onClick = {
                        onApply(
                            MediaFilters(
                                genreId = draftGenreId,
                                sortBy = draftSortBy,
                            ),
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Применить")
                }
            }
        }
    }
}

@Composable
private fun GenreDropdown(
    selectedLabel: String,
    genres: List<MediaGenre>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenreSelected: (Int?) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Жанр: $selectedLabel",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text("Все жанры") },
                onClick = {
                    onGenreSelected(null)
                    onExpandedChange(false)
                },
            )
            genres.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(genre.name) },
                    onClick = {
                        onGenreSelected(genre.id)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Composable
private fun SortSection(
    contentType: ContentType,
    draftSortBy: String?,
    onSortByChange: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Сортировка",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SortOptionRow(
            label = "По умолчанию",
            selected = draftSortBy == null,
            onClick = { onSortByChange(null) },
        )
        MediaSortOption.entries.forEach { option ->
            val sortBy = option.sortBy(contentType)
            SortOptionRow(
                label = option.label,
                selected = draftSortBy == sortBy,
                onClick = { onSortByChange(sortBy) },
            )
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
