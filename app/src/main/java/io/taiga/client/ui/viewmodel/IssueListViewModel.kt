package io.taiga.client.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IssueListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val projectId: Long = 0L,
    val projectName: String = "",
    val issues: List<IssueSummary> = emptyList(),
)

@HiltViewModel
class IssueListViewModel @Inject constructor(
    private val repository: TaigaWorkspaceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val projectId = checkNotNull(savedStateHandle.get<Long>("projectId"))
    private val projectName = savedStateHandle.get<String>("projectName") ?: "Project"

    private val _uiState = MutableStateFlow(
        IssueListUiState(
            projectId = projectId,
            projectName = projectName,
        ),
    )
    val uiState: StateFlow<IssueListUiState> = _uiState.asStateFlow()

    init {
        loadIssues()
    }

    fun loadIssues() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.loadIssues(projectId) }
                .onSuccess { issues ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            issues = issues,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load issues",
                        )
                    }
                }
        }
    }
}
