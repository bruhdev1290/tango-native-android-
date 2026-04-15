package io.taiga.client.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.taiga.client.data.models.ActivityItem
import io.taiga.client.data.models.ActivityType
import io.taiga.client.data.models.ItemKind
import io.taiga.client.data.models.ItemScope
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.preferences.AppPreferencesRepository
import io.taiga.client.data.workspace.TaigaWorkspaceRepository
import io.taiga.client.push.NotificationHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShellTab { HOME, ACTIVITY }

data class ActivityFilters(
    val showStories: Boolean = true,
    val showTasks: Boolean = true,
    val showIssues: Boolean = true,
    val showAssigned: Boolean = true,
    val showInProjects: Boolean = true,
)

data class ProjectsShellUiState(
    val selectedTab: ShellTab = ShellTab.HOME,
    val projects: List<ProjectSummary> = emptyList(),
    val myWork: List<ActivityItem> = emptyList(),
    val activityItems: List<ActivityItem> = emptyList(),
    val currentUsername: String = "",
    val currentUserId: Long = 0L,
    val loading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null,
    val activityFilters: ActivityFilters = ActivityFilters(),
    val showFiltersSheet: Boolean = false,
    val searchQuery: String = "",
)

@HiltViewModel
class ProjectsShellViewModel @Inject constructor(
    private val workspace: TaigaWorkspaceRepository,
    private val prefs: AppPreferencesRepository,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsShellUiState())
    val uiState: StateFlow<ProjectsShellUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() = load()

    fun setTab(tab: ShellTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setActivityFilters(filters: ActivityFilters) {
        _uiState.update { it.copy(activityFilters = filters, showFiltersSheet = false) }
    }

    fun showActivityFilters() {
        _uiState.update { it.copy(showFiltersSheet = true) }
    }

    fun hideActivityFilters() {
        _uiState.update { it.copy(showFiltersSheet = false) }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching {
                val user = workspace.getCurrentUser()
                val userId = user.id
                val preferences = prefs.preferencesFlow.first()

                // Load projects where user is a member
                val projectsDeferred = async { runCatching { workspace.loadProjects(member = userId) }.getOrDefault(emptyList()) }
                val assignedStoriesDeferred = async { runCatching { workspace.getAssignedStories(userId) }.getOrDefault(emptyList()) }
                val assignedTasksDeferred = async { runCatching { workspace.getAssignedTasks(userId) }.getOrDefault(emptyList()) }
                val assignedIssuesDeferred = async { runCatching { workspace.getAssignedIssues(userId) }.getOrDefault(emptyList()) }

                val projects = projectsDeferred.await()
                val assignedStories = assignedStoriesDeferred.await()
                val assignedTasks = assignedTasksDeferred.await()
                val assignedIssues = assignedIssuesDeferred.await()

                // Build assigned activity items
                val assignedItems = buildList {
                    assignedStories.forEach { story ->
                        add(ActivityItem(
                            id = story.id,
                            ref = story.ref.toLong(),
                            subject = story.subject,
                            projectId = story.project,
                            projectName = story.project_extra_info?.name ?: "",
                            projectLogoUrl = story.project_extra_info?.logo_small_url,
                            kind = ItemKind.STORY,
                            scope = ItemScope.ASSIGNED,
                            assignedToId = story.assigned_to,
                            modifiedDate = story.modified_date,
                            createdDate = story.created_date,
                            activityType = ActivityType.ASSIGNED,
                            description = "Assigned to you",
                        ))
                    }
                    assignedTasks.forEach { task ->
                        add(ActivityItem(
                            id = task.id,
                            ref = task.ref.toLong(),
                            subject = task.subject,
                            projectId = task.project,
                            projectName = task.project_extra_info?.name ?: "",
                            projectLogoUrl = task.project_extra_info?.logo_small_url,
                            kind = ItemKind.TASK,
                            scope = ItemScope.ASSIGNED,
                            assignedToId = task.assigned_to,
                            modifiedDate = task.modified_date,
                            createdDate = task.created_date,
                            activityType = ActivityType.ASSIGNED,
                            description = "Assigned to you",
                        ))
                    }
                    assignedIssues.forEach { issue ->
                        add(ActivityItem(
                            id = issue.id,
                            ref = issue.ref,
                            subject = issue.subject,
                            projectId = issue.project ?: 0L,
                            projectName = "",
                            projectLogoUrl = null,
                            kind = ItemKind.ISSUE,
                            scope = ItemScope.ASSIGNED,
                            assignedToId = null,
                            modifiedDate = null,
                            createdDate = null,
                            activityType = ActivityType.ASSIGNED,
                            description = "Assigned to you",
                        ))
                    }
                }

                // Collect project activity items
                val projectActivityItems = buildList {
                    projects.forEach { project ->
                        runCatching { workspace.getProjectStories(project.id) }.getOrDefault(emptyList()).forEach { story ->
                            val isNew = story.created_date != null && story.modified_date == story.created_date
                            val isModified = story.modified_date != null && story.modified_date != story.created_date
                            val activityType = when {
                                isNew -> ActivityType.NEW_ITEM
                                isModified -> ActivityType.CHANGED
                                else -> ActivityType.PROJECT
                            }
                            val description = when (activityType) {
                                ActivityType.NEW_ITEM -> "New story created"
                                ActivityType.CHANGED -> "Story updated"
                                else -> "In ${project.name}"
                            }
                            add(ActivityItem(
                                id = story.id,
                                ref = story.ref.toLong(),
                                subject = story.subject,
                                projectId = story.project,
                                projectName = story.project_extra_info?.name ?: project.name,
                                projectLogoUrl = story.project_extra_info?.logo_small_url ?: project.logo_small_url,
                                kind = ItemKind.STORY,
                                scope = ItemScope.PROJECT,
                                assignedToId = story.assigned_to,
                                modifiedDate = story.modified_date,
                                createdDate = story.created_date,
                                activityType = activityType,
                                description = description,
                            ))
                        }
                        runCatching { workspace.getProjectTasks(project.id) }.getOrDefault(emptyList()).forEach { task ->
                            val isNew = task.created_date != null && task.modified_date == task.created_date
                            val isModified = task.modified_date != null && task.modified_date != task.created_date
                            val activityType = when {
                                isNew -> ActivityType.NEW_ITEM
                                isModified -> ActivityType.CHANGED
                                else -> ActivityType.PROJECT
                            }
                            val description = when (activityType) {
                                ActivityType.NEW_ITEM -> "New task created"
                                ActivityType.CHANGED -> "Task updated"
                                else -> "In ${project.name}"
                            }
                            add(ActivityItem(
                                id = task.id,
                                ref = task.ref.toLong(),
                                subject = task.subject,
                                projectId = task.project,
                                projectName = task.project_extra_info?.name ?: project.name,
                                projectLogoUrl = task.project_extra_info?.logo_small_url ?: project.logo_small_url,
                                kind = ItemKind.TASK,
                                scope = ItemScope.PROJECT,
                                assignedToId = task.assigned_to,
                                modifiedDate = task.modified_date,
                                createdDate = task.created_date,
                                activityType = activityType,
                                description = description,
                            ))
                        }
                    }
                }

                // Deduplicate: prefer ASSIGNED over PROJECT; keep newest among dupes
                val deduped = deduplicateItems(assignedItems + projectActivityItems)
                val sorted = deduped.sortedByDescending { it.modifiedDate ?: it.createdDate ?: "" }

                val myWork = sorted.filter { it.scope == ItemScope.ASSIGNED && it.assignedToId == userId }

                // Post local notifications for new activity if enabled
                if (preferences.localNotificationsEnabled) {
                    val previousIds = _uiState.value.activityItems.map { it.id }.toSet()
                    val newItems = sorted.filter { it.id !in previousIds }
                    newItems.take(3).forEach { item ->
                        when {
                            item.scope == ItemScope.ASSIGNED && preferences.notifyNewAssigned -> {
                                notificationHelper.showNotification(
                                    id = item.id.toInt(),
                                    title = "Assigned: ${item.subject}",
                                    message = item.projectName,
                                    channelId = NotificationHelper.CHANNEL_ASSIGNED,
                                )
                            }
                            item.scope == ItemScope.PROJECT && preferences.notifyNewInProjects -> {
                                notificationHelper.showNotification(
                                    id = item.id.toInt(),
                                    title = "${item.projectName}",
                                    message = item.description ?: item.subject,
                                    channelId = NotificationHelper.CHANNEL_PROJECT,
                                )
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        loading = false,
                        projects = projects,
                        myWork = myWork,
                        activityItems = sorted,
                        currentUsername = user.full_name_display.ifBlank { user.username },
                        currentUserId = userId,
                        lastUpdated = System.currentTimeMillis(),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(loading = false, error = e.message ?: "Failed to load") }
            }
        }
    }

    private fun deduplicateItems(items: List<ActivityItem>): List<ActivityItem> {
        val byKey = LinkedHashMap<String, ActivityItem>()
        // Process ASSIGNED items first so they take priority over PROJECT items
        val assignedFirst = items.sortedByDescending { if (it.scope == ItemScope.ASSIGNED) 1 else 0 }
        for (item in assignedFirst) {
            val key = "${item.kind}:${item.id}"
            val existing = byKey[key]
            if (existing == null) {
                byKey[key] = item
            } else if (existing.scope == ItemScope.PROJECT && item.scope == ItemScope.ASSIGNED) {
                byKey[key] = item
            }
            // else keep existing (already preferred or same scope, keep newer via sort later)
        }
        return byKey.values.toList()
    }
}
