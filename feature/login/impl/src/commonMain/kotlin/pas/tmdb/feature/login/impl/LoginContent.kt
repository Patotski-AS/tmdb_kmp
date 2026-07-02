package pas.tmdb.feature.login.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pas.tmdb.auth.api.SessionState
import pas.tmdb.feature.login.api.LoginComponent
import pas.tmdb.feature.login.api.LoginIntent

@Composable
fun LoginContent(
    component: LoginComponent,
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(component) {
        component.browserUrlEvents.collect { url ->
            uriHandler.openUri(url)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val sessionState = model.sessionState) {
            is SessionState.Guest -> {
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

            is SessionState.Authenticated -> {
                AuthenticatedContent(
                    username = sessionState.username,
                    accountId = sessionState.accountId,
                    isLoading = model.isLoading,
                    onLogout = { component.onIntent(LoginIntent.Logout) },
                )
            }
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

@Composable
private fun AuthenticatedContent(
    username: String?,
    accountId: Int,
    isLoading: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = username?.takeIf { it.isNotBlank() } ?: "Аккаунт #$accountId",
            style = MaterialTheme.typography.titleLarge,
        )

        Text(
            text = "Вы вошли в аккаунт TMDB",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(
            onClick = onLogout,
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text("Выйти")
            }
        }
    }
}
