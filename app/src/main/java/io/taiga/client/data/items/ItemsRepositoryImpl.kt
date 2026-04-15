package io.taiga.client.data.items

import io.taiga.client.data.models.IssueSummary
import io.taiga.client.data.models.SprintSummary
import io.taiga.client.data.models.TaskSummary
import io.taiga.client.data.models.UserStorySummary
import io.taiga.client.data.network.TaigaServiceFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemsRepositoryImpl @Inject constructor(
    private val serviceFactory: TaigaServiceFactory,
) : ItemsRepository {
    override suspend fun getProjectStories(projectId: Long): List<UserStorySummary> =
        serviceFactory.createAuthenticatedApi().userStories(projectId = projectId)

    override suspend fun getProjectTasks(projectId: Long): List<TaskSummary> =
        serviceFactory.createAuthenticatedApi().tasks(projectId = projectId)

    override suspend fun getProjectIssues(projectId: Long): List<IssueSummary> =
        serviceFactory.createAuthenticatedApi().issues(projectId = projectId)

    override suspend fun getSprints(projectId: Long): List<SprintSummary> =
        serviceFactory.createAuthenticatedApi().milestones(projectId)

    override suspend fun createStory(projectId: Long, subject: String): UserStorySummary =
        serviceFactory.createAuthenticatedApi().createUserStory(
            mapOf("project" to projectId, "subject" to subject)
        )

    override suspend fun createTask(projectId: Long, subject: String): TaskSummary =
        serviceFactory.createAuthenticatedApi().createTask(
            mapOf("project" to projectId, "subject" to subject)
        )

    override suspend fun createIssue(projectId: Long, subject: String): IssueSummary =
        serviceFactory.createAuthenticatedApi().createIssue(
            mapOf("project" to projectId, "subject" to subject)
        )

    override suspend fun updateStory(id: Long, subject: String): UserStorySummary =
        serviceFactory.createAuthenticatedApi().updateUserStory(id, mapOf("subject" to subject))

    override suspend fun updateTask(id: Long, subject: String): TaskSummary =
        serviceFactory.createAuthenticatedApi().updateTask(id, mapOf("subject" to subject))

    override suspend fun updateIssue(id: Long, subject: String): IssueSummary =
        serviceFactory.createAuthenticatedApi().updateIssue(id, mapOf("subject" to subject))

    override suspend fun deleteStory(id: Long) =
        serviceFactory.createAuthenticatedApi().deleteUserStory(id)

    override suspend fun deleteTask(id: Long) =
        serviceFactory.createAuthenticatedApi().deleteTask(id)

    override suspend fun deleteIssue(id: Long) =
        serviceFactory.createAuthenticatedApi().deleteIssue(id)
}
