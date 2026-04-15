package io.taiga.client.data.items

import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserStorySummary

interface ItemsRepository {
    suspend fun getProjectStories(projectId: Long): List<UserStorySummary>
    suspend fun getProjectTasks(projectId: Long): List<TaskSummary>
    suspend fun getProjectIssues(projectId: Long): List<IssueSummary>
    suspend fun getSprints(projectId: Long): List<SprintSummary>
    suspend fun createStory(projectId: Long, subject: String): UserStorySummary
    suspend fun createTask(projectId: Long, subject: String): TaskSummary
    suspend fun createIssue(projectId: Long, subject: String): IssueSummary
    suspend fun updateStory(id: Long, subject: String): UserStorySummary
    suspend fun updateTask(id: Long, subject: String): TaskSummary
    suspend fun updateIssue(id: Long, subject: String): IssueSummary
    suspend fun deleteStory(id: Long)
    suspend fun deleteTask(id: Long)
    suspend fun deleteIssue(id: Long)
}
