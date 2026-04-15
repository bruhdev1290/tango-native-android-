package io.taiga.client.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.items.ItemsRepository
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ItemKind
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserStorySummary
import io.taiga.client.ui.navigation.Routes
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DisplayFilter { ALL, SPRINTS, STORIES, TASKS, ISSUES }

data class ProjectBacklogUiState(
    val projectName: String = "",
    val stories: List<UserStorySummary> = emptyList(),
    val tasks: List<TaskSummary> = emptyList(),
    val issues: List<IssueSummary> = emptyList(),
    val sprints: List<SprintSummary> = emptyList(),
    val searchQuery: String = "",
    val displayFilter: DisplayFilter = DisplayFilter.ALL,
    val statusFilter: String? = null,
    val assigneeFilter: Long? = null,
    val completedItemIds: Set<String> = emptySet(),
    val loading: Boolean = false,
    val error: String? = null,
    val showAddSheet: Boolean = false,
    val addSheetTarget: ItemKind? = null,
    val editItem: EditItem? = null,
)

data class EditItem(val kind: ItemKind, val id: Long, val currentSubject: String)

@HiltViewModel
class ProjectBacklogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val items: ItemsRepository,
) : ViewModel() {

    private val projectId: Long = savedStateHandle[Routes.PROJECT_ID_ARG] ?: 0L
    private val projectName: String = savedStateHandle[Routes.PROJECT_NAME_ARG] ?: ""

    private val _uiState = MutableStateFlow(ProjectBacklogUiState(projectName = projectName))
    val uiState: StateFlow<ProjectBacklogUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    fun setSearchQuery(q: String) {
        _uiState.update { it.copy(searchQuery = q) }
    }

    fun setDisplayFilter(f: DisplayFilter) {
        _uiState.update { it.copy(displayFilter = f) }
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
    }

    fun setAssigneeFilter(userId: Long?) {
        _uiState.update { it.copy(assigneeFilter = userId) }
    }

    fun showAddSheet(kind: ItemKind) {
        _uiState.update { it.copy(showAddSheet = true, addSheetTarget = kind) }
    }

    fun hideAddSheet() {
        _uiState.update { it.copy(showAddSheet = false, addSheetTarget = null) }
    }

    fun showEditSheet(kind: ItemKind, id: Long, currentSubject: String) {
        _uiState.update { it.copy(editItem = EditItem(kind, id, currentSubject)) }
    }

    fun hideEditSheet() {
        _uiState.update { it.copy(editItem = null) }
    }

    fun addStory(subject: String) {
        if (subject.isBlank()) return
        viewModelScope.launch {
            runCatching { items.createStory(projectId, subject) }.onSuccess { new ->
                _uiState.update { it.copy(stories = it.stories + new, showAddSheet = false) }
            }
        }
    }

    fun addTask(subject: String) {
        if (subject.isBlank()) return
        viewModelScope.launch {
            runCatching { items.createTask(projectId, subject) }.onSuccess { new ->
                _uiState.update { it.copy(tasks = it.tasks + new, showAddSheet = false) }
            }
        }
    }

    fun addIssue(subject: String) {
        if (subject.isBlank()) return
        viewModelScope.launch {
            runCatching { items.createIssue(projectId, subject) }.onSuccess { new ->
                _uiState.update { it.copy(issues = it.issues + new, showAddSheet = false) }
            }
        }
    }

    fun editItem(kind: ItemKind, id: Long, newSubject: String) {
        if (newSubject.isBlank()) return
        viewModelScope.launch {
            runCatching {
                when (kind) {
                    ItemKind.STORY -> {
                        val updated = items.updateStory(id, newSubject)
                        _uiState.update { state ->
                            state.copy(
                                stories = state.stories.map { if (it.id == id) updated else it },
                                editItem = null,
                            )
                        }
                    }
                    ItemKind.TASK -> {
                        val updated = items.updateTask(id, newSubject)
                        _uiState.update { state ->
                            state.copy(
                                tasks = state.tasks.map { if (it.id == id) updated else it },
                                editItem = null,
                            )
                        }
                    }
                    ItemKind.ISSUE -> {
                        val updated = items.updateIssue(id, newSubject)
                        _uiState.update { state ->
                            state.copy(
                                issues = state.issues.map { if (it.id == id) updated else it },
                                editItem = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleComplete(kind: ItemKind, id: Long) {
        val key = "${kind.name}:$id"
        _uiState.update { state ->
            val newSet = if (key in state.completedItemIds) {
                state.completedItemIds - key
            } else {
                state.completedItemIds + key
            }
            state.copy(completedItemIds = newSet)
        }
    }

    fun deleteItem(kind: ItemKind, id: Long) {
        viewModelScope.launch {
            runCatching {
                when (kind) {
                    ItemKind.STORY -> {
                        items.deleteStory(id)
                        _uiState.update { it.copy(stories = it.stories.filter { s -> s.id != id }) }
                    }
                    ItemKind.TASK -> {
                        items.deleteTask(id)
                        _uiState.update { it.copy(tasks = it.tasks.filter { t -> t.id != id }) }
                    }
                    ItemKind.ISSUE -> {
                        items.deleteIssue(id)
                        _uiState.update { it.copy(issues = it.issues.filter { i -> i.id != id }) }
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                val storiesDeferred = async { runCatching { items.getProjectStories(projectId) }.getOrDefault(emptyList()) }
                val tasksDeferred = async { runCatching { items.getProjectTasks(projectId) }.getOrDefault(emptyList()) }
                val issuesDeferred = async { runCatching { items.getProjectIssues(projectId) }.getOrDefault(emptyList()) }
                val sprintsDeferred = async { runCatching { items.getSprints(projectId) }.getOrDefault(emptyList()) }

                _uiState.update {
                    it.copy(
                        loading = false,
                        stories = storiesDeferred.await(),
                        tasks = tasksDeferred.await(),
                        issues = issuesDeferred.await(),
                        sprints = sprintsDeferred.await(),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }
}
