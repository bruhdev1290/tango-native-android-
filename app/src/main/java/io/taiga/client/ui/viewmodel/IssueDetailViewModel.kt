package io.taiga.client.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssueDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val issue: IssueDetail? = null,
)

@HiltViewModel
class IssueDetailViewModel @Inject constructor(
    private val repository: TaigaWorkspaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val issueId = checkNotNull(savedStateHandle.get<Long>("issueId"))

    private val _uiState = MutableStateFlow(IssueDetailUiState())
    val uiState: StateFlow<IssueDetailUiState> = _uiState.asStateFlow()

    init {
        loadIssue()
    }

    fun loadIssue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.loadIssue(issueId) }
                .onSuccess { issue ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            issue = issue,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load issue",
                        )
                    }
                }
        }
    }
}
