package io.taiga.client.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.taiga.client.data.auth.AuthState
import io.taiga.client.data.auth.TaigaAuthError
import io.taiga.client.ui.viewmodel.LoginFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    authState: AuthState,
    formState: LoginFormState,
    onBaseUrlChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onGitHubCodeChanged: (String) -> Unit,
    onNormalLoginClick: () -> Unit,
    onGitHubLoginClick: () -> Unit,
    onClearError: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Taiga Client",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Connect to a Taiga instance and sign in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Base URL field - common for both login methods
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formState.baseUrl,
                    onValueChange = onBaseUrlChanged,
                    label = { Text("Taiga base URL") },
                    placeholder = { Text("https://api.taiga.io/api/v1/") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                    ),
                    enabled = authState !is AuthState.Loading,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Normal Login Section
                NormalLoginSection(
                    username = formState.username,
                    password = formState.password,
                    onUsernameChanged = onUsernameChanged,
                    onPasswordChanged = onPasswordChanged,
                    onLoginClick = onNormalLoginClick,
                    isLoading = authState is AuthState.Loading,
                    enabled = authState !is AuthState.Loading,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // GitHub Login Section
                GitHubLoginSection(
                    code = formState.gitHubCode,
                    onCodeChanged = onGitHubCodeChanged,
                    onLoginClick = onGitHubLoginClick,
                    isLoading = authState is AuthState.Loading,
                    enabled = authState !is AuthState.Loading,
                )

                // Error display
                if (authState is AuthState.Failed) {
                    ErrorMessage(
                        error = authState.error,
                        message = authState.message,
                        onDismiss = onClearError,
                    )
                }
            }
        }
    }
}

@Composable
private fun NormalLoginSection(
    username: String,
    password: String,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Login with Username",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text("Username or email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            enabled = enabled,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            enabled = enabled,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLoginClick,
            enabled = enabled && username.isNotBlank() && password.isNotBlank(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 12.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            }
            Text(if (isLoading) "Signing in..." else "Sign in")
        }
    }
}

@Composable
private fun GitHubLoginSection(
    code: String,
    onCodeChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
) {
    var showGitHubLogin by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Or login with GitHub",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        if (!showGitHubLogin) {
            TextButton(
                onClick = { showGitHubLogin = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Use GitHub OAuth")
            }
        } else {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = code,
                onValueChange = onCodeChanged,
                label = { Text("GitHub OAuth Code") },
                placeholder = { Text("Paste authorization code from GitHub") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                ),
                enabled = enabled,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = { 
                        showGitHubLogin = false
                        onCodeChanged("")
                    },
                    enabled = enabled,
                ) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onLoginClick,
                    enabled = enabled && code.isNotBlank(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 12.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    }
                    Text(if (isLoading) "Authenticating..." else "Login with GitHub")
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    error: TaigaAuthError?,
    message: String,
    onDismiss: () -> Unit,
) {
    val displayMessage = when (error) {
        is TaigaAuthError.InvalidCredentials -> "Invalid username or password. Please try again."
        is TaigaAuthError.GitHubAuthFailed -> "GitHub authentication failed: ${error.detail}"
        is TaigaAuthError.HttpError -> "Server error (${error.status}). Please try again later."
        is TaigaAuthError.NetworkError -> "Network error. Please check your connection."
        else -> message
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
            )
            .padding(12.dp),
    ) {
        Text(
            text = displayMessage,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Dismiss", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
