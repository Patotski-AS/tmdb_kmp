package pas.tmdb.feature.details.api

import com.arkivanov.decompose.value.Value

interface DetailsComponent {
    val model: Value<DetailsModel>
    fun onIntent(intent: DetailsIntent)
}

data class DetailsModel(
    val title: String? = null,
    val overview: String? = null,
    val posterUrl: String? = null,
    val voteAverage: Double? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null,
    val genres: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface DetailsIntent {
    data object Load : DetailsIntent
    data object Retry : DetailsIntent
    data object Back : DetailsIntent
}
