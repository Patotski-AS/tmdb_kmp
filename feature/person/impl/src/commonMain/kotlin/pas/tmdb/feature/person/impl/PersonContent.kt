package pas.tmdb.feature.person.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.core.ui.DetailsSectionTitle
import pas.tmdb.core.ui.MediaPosterItem
import pas.tmdb.core.ui.PosterPlaceholder
import pas.tmdb.core.ui.RecommendationPosterRow
import pas.tmdb.core.ui.ShimmerDetails
import pas.tmdb.core.ui.TmdbErrorState
import pas.tmdb.feature.person.api.PersonComponent
import pas.tmdb.feature.person.api.PersonCreditModel
import pas.tmdb.feature.person.api.PersonIntent
import pas.tmdb.feature.person.api.PersonModel

private const val CREDITS_PREVIEW_COUNT: Int = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonContent(
    component: PersonComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(model.name ?: "Персона") },
                navigationIcon = {
                    IconButton(onClick = { component.onIntent(PersonIntent.Back) }) {
                        Text("←")
                    }
                },
            )
        },
    ) { padding ->
        when {
            model.isLoading && model.name == null -> {
                ShimmerDetails(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            model.error != null && model.name == null -> {
                TmdbErrorState(
                    message = model.error.orEmpty(),
                    onRetry = { component.onIntent(PersonIntent.Retry) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            else -> {
                PersonBody(
                    model = model,
                    onIntent = component::onIntent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}

@Composable
private fun PersonBody(
    model: PersonModel,
    onIntent: (PersonIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PersonHeader(model = model)

        model.biography?.takeIf { it.isNotBlank() }?.let { biography ->
            DetailsSectionTitle(title = "Биография")
            Text(
                text = biography,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (model.knownFor.isNotEmpty()) {
            DetailsSectionTitle(title = "Известен по")
            RecommendationPosterRow(
                items = model.knownFor.map { it.toPosterItem() },
                onItemClick = { index ->
                    val credit = model.knownFor[index]
                    onIntent(PersonIntent.OnCreditClick(credit.id, credit.mediaType))
                },
            )
        }

        if (model.castCredits.isNotEmpty()) {
            CreditsSection(
                title = "Роли",
                credits = model.castCredits,
                showFull = model.showFullCastCredits,
                onShowFull = { onIntent(PersonIntent.ShowFullCastCredits) },
                onCreditClick = { credit ->
                    onIntent(PersonIntent.OnCreditClick(credit.id, credit.mediaType))
                },
            )
        }

        if (model.crewCredits.isNotEmpty()) {
            CreditsSection(
                title = "Команда",
                credits = model.crewCredits,
                showFull = model.showFullCrewCredits,
                onShowFull = { onIntent(PersonIntent.ShowFullCrewCredits) },
                onCreditClick = { credit ->
                    onIntent(PersonIntent.OnCreditClick(credit.id, credit.mediaType))
                },
            )
        }
    }
}

@Composable
private fun PersonHeader(model: PersonModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
        ) {
            if (model.profileUrl.isNullOrBlank()) {
                PosterPlaceholder(modifier = Modifier.fillMaxSize())
            } else {
                SubcomposeAsyncImage(
                    model = model.profileUrl,
                    contentDescription = model.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { PosterPlaceholder(modifier = Modifier.fillMaxSize()) },
                    error = { PosterPlaceholder(modifier = Modifier.fillMaxSize()) },
                    success = { SubcomposeAsyncImageContent() },
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = model.name.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            model.knownForDepartment?.let { department ->
                Text(
                    text = department,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            model.birthday?.let { birthday ->
                Text(
                    text = if (model.deathday != null) {
                        "$birthday — ${model.deathday}"
                    } else {
                        "Дата рождения: $birthday"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            model.placeOfBirth?.let { place ->
                Text(
                    text = place,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (model.alsoKnownAs.isNotEmpty()) {
                Text(
                    text = model.alsoKnownAs.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CreditsSection(
    title: String,
    credits: List<PersonCreditModel>,
    showFull: Boolean,
    onShowFull: () -> Unit,
    onCreditClick: (PersonCreditModel) -> Unit,
) {
    val visible = if (showFull) credits else credits.take(CREDITS_PREVIEW_COUNT)

    DetailsSectionTitle(title = title)
    RecommendationPosterRow(
        items = visible.map { it.toPosterItem() },
        onItemClick = { index -> onCreditClick(visible[index]) },
    )

    if (!showFull && credits.size > CREDITS_PREVIEW_COUNT) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onShowFull) {
                Text("Смотреть ещё")
            }
        }
    }
}

private fun PersonCreditModel.toPosterItem(): MediaPosterItem =
    MediaPosterItem(
        id = id,
        title = title,
        posterUrl = posterUrl,
        voteAverage = voteAverage,
    )
