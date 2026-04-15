package io.taiga.client.data.workspace

import io.taiga.client.data.models.IssueDetail
import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.ProjectSummary
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserMe
import io.taiga.client.data.models.UserStorySummary

interface TaigaWorkspaceRepository {
    suspend fun loadProjects(member: Long? = null): List<ProjectSummary>
    suspend fun loadIssues(projectId: Long): List<IssueSummary>
    suspend fun loadIssue(issueId: Long): IssueDetail
    suspend fun getCurrentUser(): UserMe
    suspend fun getAssignedStories(userId: Long): List<UserStorySummary>
    suspend fun getAssignedTasks(userId: Long): List<TaskSummary>
    suspend fun getAssignedIssues(userId: Long): List<IssueSummary>
    suspend fun getProjectStories(projectId: Long): List<UserStorySummary>
    suspend fun getProjectTasks(projectId: Long): List<TaskSummary>
    suspend fun getSprints(projectId: Long): List<SprintSummary>
}
