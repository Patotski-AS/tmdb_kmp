package pas.tmdb.feature.media.api

import com.arkivanov.decompose.value.Value

interface MediaComponent {
    val model: Value<MediaModel>
    fun onIntent(intent: MediaIntent)
}

data class MediaModel(
    val contentType: ContentType = ContentType.Movies,
    val searchQuery: String = "",
    val filters: MediaFilters = MediaFilters(),
    val genres: List<MediaGenre> = emptyList(),
    val items: List<MediaItemModel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val favoriteIds: Set<Int> = emptySet(),
)

sealed interface MediaIntent {
    data object Load : MediaIntent
    data object LoadMore : MediaIntent
    data class OnItemClick(val id: Int) : MediaIntent
    data class OnContentTypeSelected(val contentType: ContentType) : MediaIntent
    data class QueryChanged(val query: String) : MediaIntent
    data object ClearQuery : MediaIntent
    data class ApplyFilters(val filters: MediaFilters) : MediaIntent
    data object ResetFilters : MediaIntent
    data object OnProfileClick : MediaIntent
}
