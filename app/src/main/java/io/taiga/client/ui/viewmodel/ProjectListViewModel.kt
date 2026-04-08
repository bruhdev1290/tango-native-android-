package io.taiga.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val visibilityFilter: ProjectVisibilityFilter = ProjectVisibilityFilter.ALL,
    val projects: List<ProjectSummary> = emptyList(),
)

enum class ProjectVisibilityFilter {
    ALL,
    PUBLIC,
    PRIVATE,
}

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val repository: TaigaWorkspaceRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching { repository.loadProjects() }
                .onSuccess { projects ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            projects = projects,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load projects",
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onVisibilityFilterChanged(filter: ProjectVisibilityFilter) {
        _uiState.update { it.copy(visibilityFilter = filter) }
    }
}
