package io.taiga.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.auth.AuthRepository
import io.taiga.client.data.session.UserSession
import io.taiga.client.data.session.SecureSessionStore
import io.taiga.client.data.session.normalizeTaigaApiBaseUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val DEFAULT_BASE_URL = "https://api.taiga.io/api/v1/"

data class AuthUiState(
    val isLoading: Boolean = true,
    val inProgress: Boolean = false,
    val apiBaseUrl: String = DEFAULT_BASE_URL,
    val username: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val session: UserSession? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SecureSessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = authRepository.restoreSession()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    apiBaseUrl = restored?.baseUrl ?: DEFAULT_BASE_URL,
                    session = restored,
                )
            }
        }

        viewModelScope.launch {
            sessionStore.observeSession().collect { session ->
                _uiState.update { current ->
                    current.copy(
                        session = session,
                        apiBaseUrl = session?.baseUrl ?: current.apiBaseUrl,
                    )
                }
            }
        }
    }

    fun onBaseUrlChanged(value: String) {
        _uiState.update { it.copy(apiBaseUrl = value, errorMessage = null) }
    }

    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState.inProgress) return

        viewModelScope.launch {
            _uiState.update { it.copy(inProgress = true, errorMessage = null) }

            runCatching {
                authRepository.login(
                    baseUrl = normalizeTaigaApiBaseUrl(currentState.apiBaseUrl),
                    username = currentState.username.trim(),
                    password = currentState.password,
                )
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        inProgress = false,
                        session = session,
                        apiBaseUrl = session.baseUrl,
                        password = "",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        inProgress = false,
                        errorMessage = throwable.message ?: "Login failed",
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    session = null,
                    password = "",
                    errorMessage = null,
                )
            }
        }
    }

}
